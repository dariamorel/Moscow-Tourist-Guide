#include "route.hpp"

#include <userver/formats/json/value_builder.hpp>

namespace tour_guide {

userver::formats::json::Value
Serialize(const Route& route, userver::formats::serialize::To<userver::formats::json::Value>) {
    userver::formats::json::ValueBuilder builder;
    builder["id"] = route.id;
    builder["order"] = route.order;
    builder["planned_order"] = route.planned_order;
    builder["user_id"] = route.user_id;
    builder["total_time"] = route.total_time;
    builder["date"] = route.date;

    return builder.ExtractValue();
}

}  //  namespace tour_guide
