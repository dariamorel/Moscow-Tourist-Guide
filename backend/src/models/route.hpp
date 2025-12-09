#pragma once

#include <string>
#include <vector>
#include <chrono>

#include <userver/formats/json_fwd.hpp>

namespace tour_guide {

struct Route {
    int id;
    std::vector<int> order;
    std::vector<int> planned_order;
    int user_id;
    std::chrono::system_clock::duration total_time;
    std::chrono::system_clock::time_point date;
};

userver::formats::json::Value Serialize(const Route& route, userver::formats::serialize::To<userver::formats::json::Value>);

}  // namespace tour_guide