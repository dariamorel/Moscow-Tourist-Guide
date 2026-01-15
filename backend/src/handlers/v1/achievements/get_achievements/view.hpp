#pragma once

#include <userver/components/component.hpp>
#include <userver/server/handlers/http_handler_base.hpp>

#include <userver/storages/postgres/cluster.hpp>

#include <fmt/format.h>

namespace tour_guide {

class GetAchievementsHandler final : public userver::server::handlers::HttpHandlerBase {
public:
    static constexpr std::string_view kName = "get-achievements";

    GetAchievementsHandler(const userver::components::ComponentConfig& config,
        const userver::components::ComponentContext& component_context);

    std::string HandleRequestThrow(const userver::server::http::HttpRequest& request,
        userver::server::request::RequestContext&) const override;

private:
    userver::storages::postgres::ClusterPtr pg_cluster_;
};

}  // namespace tour_guide
