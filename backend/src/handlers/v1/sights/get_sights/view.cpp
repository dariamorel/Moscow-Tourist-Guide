#include "view.hpp"

#include "../../../../models/sight.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

GetSightsHandler::GetSightsHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context, false),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string GetSightsHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    auto request_result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "SELECT id, img, lat, lon, tag, short_desc, desc, timetable, addr, phone, subway "
        "FROM tour_guide.sights"
    );

    if (request_result.IsEmpty()) {
        request.SetResponseStatus(userver::server::http::HttpStatus::kNotFound);
        return "";
    }

    std::vector<Sight> sights;
    sights.reserve(request_result.Size());
    for (auto row : request_result) {
        Sight sight;
        sight.id = row["id"].As<int>();
        sight.img = row["img"].As<std::string>();
        sight.lat = row["lat"].As<double>();
        sight.lon = row["lon"].As<double>();
        sight.tag = row["tag"].As<std::vector<std::string>>();
        sight.short_desc = row["short_desc"].As<std::string>();
        sight.desc = row["desc"].As<std::string>();
        sight.timetable = row["timetable"].As<std::string>();
        sight.addr = row["addr"].As<std::string>();
        sight.phone = row["phone"].As<std::string>();
        sight.subway = row["subway"].As<std::string>();

        sights.push_back(std::move(sight));
    }

    userver::formats::json::ValueBuilder json;
    for (const auto& sight : sights) {
        json["sights"].PushBack(userver::formats::json::ValueBuilder{sight});
    }

    return ToString(json.ExtractValue());
}

}  // namespace tour_guide
