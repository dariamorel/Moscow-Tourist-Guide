#include "view.hpp"

#include "../../../../models/route.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>
#include <vector>

namespace tour_guide {

UpdateRouteHandler::UpdateRouteHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string UpdateRouteHandler::HandleRequestThrow(
    const userver::server::http::HttpRequest& request,
    userver::server::request::RequestContext&
) const {
    using userver::formats::json::FromString;
    using userver::server::http::HttpStatus;

    auto route_id_str = request.GetPathArg("routeId");
    int route_id = 0;
    try {
        route_id = std::stoi(route_id_str);
    } catch (...) {
        request.SetResponseStatus(HttpStatus::kBadRequest);
        return "";
    }

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
        !body.HasMember("date") || 
        !body.HasMember("status")) {
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
    const auto status = body["status"].As<std::string>();

    auto result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "UPDATE tour_guide.routes SET "
        "order = $1, "
        "planned_order = $2, "
        "user_id = $3, "
        "total_time = $4, "
        "date = $5, "
        "status = $6 "
        "WHERE id = $7 "
        "RETURNING id",
        order,
        planned_order,
        user_id,
        total_time,
        date,
        status,
        route_id
    );

    if (result.IsEmpty()) {
        request.SetResponseStatus(HttpStatus::kNotFound);
        return "";
    }

    Route route;
    route.id = route_id;
    route.order = order;
    route.planned_order = planned_order;
    route.user_id = user_id;
    route.total_time = total_time;
    route.date = date;
    route.status = status;

    request.SetResponseStatus(HttpStatus::kOk);
    userver::formats::json::ValueBuilder json{route};
    return ToString(json.ExtractValue());
}

}  // namespace tour_guide
