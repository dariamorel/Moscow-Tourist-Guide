#pragma once

#include <string>
#include <vector>

#include <userver/formats/json_fwd.hpp>

namespace tour_guide {

struct Sight {
    int id;
    std::string img;
    double lat;
    double lon;
    std::vector<std::string> tag;
    std::string short_desc;
    std::string desc;
    std::string timetable;
    std::string addr;
    std::string phone;
    std::string subway;
};

userver::formats::json::Value Serialize(const Sight& sight, userver::formats::serialize::To<userver::formats::json::Value>);

}  // namespace tour_guide