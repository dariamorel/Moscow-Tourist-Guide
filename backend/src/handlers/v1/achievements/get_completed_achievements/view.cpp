#include "view.hpp"

#include "../../../../models/achievement.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

GetCompletedAchievementsHandler::GetCompletedAchievementsHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string GetCompletedAchievementsHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    auto user_id_str = request.GetArg("userId");
    int user_id = 0;
    try {
        user_id = std::stoi(user_id_str);
    } catch (...) {
        return userver::server::http::HttpResponse{}
            .SetStatus(userver::server::http::HttpStatus::kBadRequest)
            .SetBody("Invalid user Id");
    }

    auto request_result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "SELECT * "
        "FROM tour_guide.achievement "
        "WHERE id IN ( "
        "    SELECT achievement_id "
        "    FROM tour_guide.user_to_achievement "
        "    WHERE user_id = $1 "
        "); ",
        user_id
    );

    if (request_result.IsEmpty()) {
        return "";
    }

    std::vector<Achievement> achievements;
    achievements.reserve(request_result.size());
    for (auto row : request_result) {
        Achievement achievement;
        achievement.id = row["id"].As<int>();
        achievement.type = row["type"].As<int>();
        achievement.count = row["count"].As<int>();
        achievement.tag = row["tag"].As<std::vector<std::string>>();

        achievements.push_back(std::move(achievement));
    }

    userver::formats::json::ValueBuilder json;
    auto& arr = json["achievements"];

    for (const auto& achievement : achievements) {
        arr.PushBack(userver::formats::json::ValueBuilder{achievement});
    }

    return ToString(json.ExtractValue());
}

}  // namespace tour_guide
