#include "view.hpp"

#include "../../../../models/sight.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

GetInProgressRoutesHandler::GetInProgressRoutesHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string GetInProgressRoutesHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    auto user_id_str = request.GetArg("user_id");
    int user_id = 0;
    try {
        user_id = std::stoi(user_id_str);
    } catch (...) {
        request.SetStatus(userver::server::http::HttpStatus::kBadRequest)
        return "";
    }

    auto request_result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "SELECT id, order, planned_order, total_time, date "
        "FROM tour_guide.routes "
        "WHERE user_id = $1 AND status = $2 ",
        user_id, "in_progress"
    );

    if (request_result.IsEmpty()) {
        return "";
    }

    std::vector<Route> routes;
    routes.reserve(request_result.size());
    for (auto row : request_result) {
        Route route;
        route.id = row["id"].As<int>();
        route.order = row["order"].As<std::vector<int>>();
        route.planned_order = row["planned_order"].As<std::vector<int>>();
        route.user_id = user_id;
        route.total_time = row["total_time"].As<std::chrono::system_clock::duration>();
        route.date = row["date"].As<std::chrono::system_clock::time_point>();

        routes.push_back(std::move(sight));
    }

    userver::formats::json::ValueBuilder json;
    auto& arr = json["routes"];

    for (const auto& route : routes) {
        arr.PushBack(userver::formats::json::ValueBuilder{route});
    }

    return ToString(json.ExtractValue());
}

}  // namespace tour_guide
