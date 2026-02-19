#include "view.hpp"

#include "../../../../models/sight.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

GetSightHandler::GetSightHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context, false),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string GetSightHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    auto sight_id_str = request.GetPathArg("sightId");
    int sight_id = 0;
    try {
        sight_id = std::stoi(sight_id_str);
    } catch (...) {
        request.SetResponseStatus(userver::server::http::HttpStatus::kBadRequest);
        return "";
    }

    auto result = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "SELECT img, lat, lon, tag, short_desc, desc, timetable, addr, phone, subway "
        "FROM tour_guide.sights "
        "WHERE id = $1 ",
        sight_id
    );

    if (result.IsEmpty()) {
        request.SetResponseStatus(userver::server::http::HttpStatus::kNotFound);
        return "";
    }
    const auto row = result[0];

    Sight sight;
    sight.id = sight_id;
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

    request.SetResponseStatus(userver::server::http::HttpStatus::kOk);
    return ToString(userver::formats::json::ValueBuilder{sight}.ExtractValue());
}

}  // namespace tour_guide
