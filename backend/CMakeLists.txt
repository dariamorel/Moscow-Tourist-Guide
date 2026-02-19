cmake_minimum_required(VERSION 3.15)
project(tour_guide CXX)

find_package(userver REQUIRED COMPONENTS
    core
    postgresql
)

add_executable(tour_guide
    src/main.cpp
    src/handlers/v1/sights/get_sight/view.cpp
    src/handlers/v1/sights/get_sights/view.cpp
    src/models/sight.cpp
)

target_link_libraries(tour_guide
    userver::core
    userver::postgresql
)

set_target_properties(tour_guide PROPERTIES
    CXX_STANDARD 20
)