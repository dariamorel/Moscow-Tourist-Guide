#include "view.hpp"

#include "../../../../models/achievement.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

UpdateAchievementsHandler::UpdateAchievementsHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string UpdateAchievementsHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    userver::formats::json::Value body;
    try {
        body = userver::formats::json::FromString(request.RequestBody());
    } catch (...) {
        request.SetResponseStatus(userver::server::http::HttpStatus::kBadRequest);
        return "";
    }

    if (!body.HasMember("user_id") ||
        !body.HasMember("achievement_id")) {
        request.SetResponseStatus(HttpStatus::kBadRequest);
        return "";
    }
    const auto user_id = body["user_id"].As<int>();
    const auto achievement_id = body["achievement_id"].As<int>();

    auto request_result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "INSERT INTO tour_guide.user_to_achievement "
        "(user_id, achievement_id) "
        "VALUES ($1, $2) "
        "RETURNING id",
        user_id,
        achievement_id
    );

    return "";
}

}  // namespace tour_guide
