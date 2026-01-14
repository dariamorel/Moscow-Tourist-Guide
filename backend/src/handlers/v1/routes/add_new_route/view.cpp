#include "view.hpp"

#include "../../../../models/route.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>
#include <vector>

namespace tour_guide {

AddNewRouteHandler::AddNewRouteHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string AddNewRouteHandler::HandleRequestThrow(
    const userver::server::http::HttpRequest& request,
    userver::server::request::RequestContext&
) const {
    using userver::formats::json::FromString;
    using userver::server::http::HttpStatus;

    userver::formats::json::Value body;
    try {
        body = FromString(request.RequestBody());
    } catch (...) {
        request.SetResponseStatus(HttpStatus::kBadRequest);
        return "";
    }

    if (!body.HasMember("order") ||
        !body.HasMember("planned_order") ||
        !body.HasMember("user_id") ||
        !body.HasMember("total_time") ||
        !body.HasMember("date")) {
        request.SetResponseStatus(HttpStatus::kBadRequest);
        return "";
    }

    const auto order = body["order"].As<std::vector<int>>();
    const auto planned_order = body["planned_order"].As<std::vector<int>>();
    const int user_id = body["user_id"].As<int>();
    const auto total_time =
        body["total_time"].As<std::chrono::system_clock::duration>();
    const auto date =
        body["date"].As<std::chrono::system_clock::time_point>();

    auto result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "INSERT INTO tour_guide.routes "
        "(order, planned_order, user_id, total_time, date, status) "
        "VALUES ($1, $2, $3, $4, $5) "
        "RETURNING id",
        order,
        planned_order,
        user_id,
        total_time,
        date,
        "in_progress"
    );

    if (result.IsEmpty()) {
        request.SetResponseStatus(HttpStatus::kInternalServerError);
        return "";
    }

    Route route;
    route.id = result[0]["id"].As<int>();
    route.order = order;
    route.planned_order = planned_order;
    route.user_id = user_id;
    route.total_time = total_time;
    route.date = date;
    route.status = "in_progress";

    request.SetResponseStatus(HttpStatus::kCreated);
    userver::formats::json::ValueBuilder json{route};
    return ToString(json.ExtractValue());
}

}  // namespace tour_guide
