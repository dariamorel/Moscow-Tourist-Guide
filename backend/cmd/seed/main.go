package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Coords struct {
	Lat float64 `json:"lat"`
	Lon float64 `json:"lon"`
}

type Image struct {
 	Image string `json:"image"`
}

type SightJSON struct {
	ID          int64    `json:"id"`
	Title       string   `json:"title"`
	Address     string   `json:"address"`
	Timetable   string   `json:"timetable"`
	Phone       string   `json:"phone"`
	Description string   `json:"description"`
	BodyText    string   `json:"body_text"`
	Coords      Coords   `json:"coords"`
	Subway      string   `json:"subway"`
	Images      []Image  `json:"images"`
	Categories  []string `json:"categories"`
}

func main() {
	ctx := context.Background()

	dbURL := fmt.Sprintf(
	"postgres://%s:%s@%s:%s/%s?sslmode=disable",
	getEnv("DB_USER", "tourguide"),
	getEnv("DB_PASSWORD", "secret"),
	getEnv("DB_HOST", "localhost"),
	getEnv("DB_PORT", "5433"),
	getEnv("DB_NAME", "tourguide"),
	)

	db, err := pgxpool.New(ctx, dbURL)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()

	if err := db.Ping(ctx); err != nil {
		log.Fatalf("Failed to ping database: %v", err)
	}
	log.Println("Connected to database")

	jsonPath := getEnv("SEED_FILE", "sightsData/sights_list.json")
	data, err := os.ReadFile(jsonPath)
	if err != nil {
		log.Fatalf("Failed to read JSON file %s: %v", jsonPath, err)
	}

	var sights []SightJSON
	if err := json.Unmarshal(data, &sights); err != nil {
		log.Fatalf("Failed to parse JSON: %v", err)
	}
	log.Printf("Loaded %d sights from JSON", len(sights))

	imported := 0
	tagsCreated := 0
	linksCreated := 0

	for _, s := range sights {
		if s.Coords.Lat == 0 && s.Coords.Lon == 0 {
			log.Printf("Skipping sight %d (%q): no coordinates", s.ID, s.Title)
			continue
		}

		img := ""
		if len(s.Images) > 0 {
			img = s.Images[0].Image
		}

		fullDesc := s.Description
		if s.BodyText != "" {
			fullDesc = s.BodyText
		}

		_, err := db.Exec(ctx, `
			INSERT INTO tour_guide.sights (id, img, lat, lon, short_desc, full_desc, timetable, addr, phone, subway)
			VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
			ON CONFLICT (id) DO NOTHING
			`, s.ID, img, s.Coords.Lat, s.Coords.Lon,
			trim(s.Title, 200), trim(fullDesc, 5000),
			trim(s.Timetable, 200), trim(s.Address, 300),
			trim(s.Phone, 50), trim(s.Subway, 100),
		)
		if err != nil {
			log.Printf("Failed to insert sight %d: %v", s.ID, err)
			continue
		}
		imported++

		for _, cat := range s.Categories {
			cat = trim(cat, 50)
			if cat == "" {
				continue
			}

			var tagID int64
			err := db.QueryRow(ctx, `
				INSERT INTO tour_guide.tags (name) VALUES ($1)
				ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
				RETURNING id
			`, cat).Scan(&tagID)
			if err != nil {
				log.Printf("Failed to insert tag %q: %v", cat, err)
				continue
			}
			tagsCreated++

			_, err = db.Exec(ctx, `
				INSERT INTO tour_guide.sight_to_tag (sight_id, tag_id)
				VALUES ($1, $2)
				ON CONFLICT (sight_id, tag_id) DO NOTHING
			`, s.ID, tagID)
			if err != nil {
				log.Printf("Failed to link sight %d with tag %q: %v", s.ID, cat, err)
				continue
			}
			linksCreated++
		}
	}

	log.Printf("Import complete:")
	log.Printf("   - Sights inserted: %d", imported)
	log.Printf("   - Tags processed: %d", tagsCreated)
	log.Printf("   - Sight-Tag links: %d", linksCreated)

	log.Println("Generating travel time edges between all sights...")
	_, err = db.Exec(ctx, `
		INSERT INTO tour_guide.time_between_sights (sight1_id, sight2_id, travel_time, distance_meters)
		SELECT 
		s1.id AS sight1_id,
		s2.id AS sight2_id,
		(make_interval(mins => 
			ROUND(
			(6371 * 2 * ASIN(SQRT(
			POWER(SIN((s2.lat - s1.lat) * PI() / 360), 2) +
			COS(s1.lat * PI() / 180) * COS(s2.lat * PI() / 180) *
			POWER(SIN((s2.lon - s1.lon) * PI() / 360), 2)
			))) * 180 / 5 * 60
			)::int
		)) AS travel_time,
		ROUND(
			6371 * 2 * ASIN(SQRT(
			POWER(SIN((s2.lat - s1.lat) * PI() / 360), 2) +
			COS(s1.lat * PI() / 180) * COS(s2.lat * PI() / 180) *
			POWER(SIN((s2.lon - s1.lon) * PI() / 360), 2)
			)) * 1000
		)::int AS distance_meters
		FROM tour_guide.sights s1
		CROSS JOIN tour_guide.sights s2
		WHERE s1.id != s2.id
		ON CONFLICT (sight1_id, sight2_id) DO NOTHING
	`)
	if err != nil {
		log.Printf("Failed to generate edges: %v", err)
	} else {
		var edgeCount int
		_ = db.QueryRow(ctx, "SELECT COUNT(*) FROM tour_guide.time_between_sights").Scan(&edgeCount)
		log.Printf("Generated %d travel time edges", edgeCount)
	}

	log.Println("Seeding achievements...")
	
	sightsAchievements := []struct {
		Name         string
		Description  string
		Required     int
		IconURL      string
	}{
		{
			Name:        "Первые шаги",
			Description: "Посетите 5 достопримечательностей",
			Required:    5,
			IconURL:     "https://example.com/icons/step.png",
		},
		{
			Name:        "Любопытный",
			Description: "Посетите 10 достопримечательностей",
			Required:    10,
			IconURL:     "https://example.com/icons/curious.png",
		},
		{
			Name:        "Путешественник",
			Description: "Посетите 20 достопримечательностей",
			Required:    20,
			IconURL:     "https://example.com/icons/traveler.png",
		},
		{
			Name:        "Исследователь",
			Description: "Посетите 30 достопримечательностей",
			Required:    30,
			IconURL:     "https://example.com/icons/explorer.png",
		},
		{
			Name:        "Знаток Москвы",
			Description: "Посетите 50 достопримечательностей",
			Required:    50,
			IconURL:     "https://example.com/icons/expert.png",
		},
		{
			Name:        "Легенда",
			Description: "Посетите 100 достопримечательностей",
			Required:    100,
			IconURL:     "https://example.com/icons/legend.png",
		},
	}

	for _, ach := range sightsAchievements {
		_, err := db.Exec(ctx, `
		INSERT INTO tour_guide.achievements (type, name, description, icon_url, required_count, tag)
		VALUES ('sights_visited', $1, $2, $3, $4, NULL)
		ON CONFLICT DO NOTHING
		`, ach.Name, ach.Description, ach.IconURL, ach.Required)
		if err != nil {
			log.Printf("Failed to insert achievement %q: %v", ach.Name, err)
		}
	}
	log.Printf("Inserted %d 'sights_visited' achievements", len(sightsAchievements))

	tagAchievements := []struct {
		Tag          string
		Name         string
		Description  string
		Required     int
		IconURL      string
	}{
		{
			Tag:         "museums",
			Name:        "Ценитель искусства",
			Description: "Посетите 5 музеев",
			Required:    5,
			IconURL:     "https://example.com/icons/museum.png",
		},
		{
			Tag:         "restaurants",
			Name:        "Гурман",
			Description: "Посетите 5 ресторанов",
			Required:    5,
			IconURL:     "https://example.com/icons/restaurant.png",
		},
		{
			Tag:         "parks",
			Name:        "Натуралист",
			Description: "Посетите 5 парков",
			Required:    5,
			IconURL:     "https://example.com/icons/park.png",
		},
		{
			Tag:         "theatre",
			Name:        "Театрал",
			Description: "Посетите 5 театров",
			Required:    5,
			IconURL:     "https://example.com/icons/theatre.png",
		},
		{
			Tag:         "monastery",
			Name:        "Паломник",
			Description: "Посетите 5 монастырей",
			Required:    5,
			IconURL:     "https://example.com/icons/monastery.png",
		},
	}

	for _, ach := range tagAchievements {
		_, err := db.Exec(ctx, `
		INSERT INTO tour_guide.achievements (type, name, description, icon_url, required_count, tag)
		VALUES ('tag_collector', $1, $2, $3, $4, $5)
		ON CONFLICT DO NOTHING
		`, ach.Name, ach.Description, ach.IconURL, ach.Required, ach.Tag)
		if err != nil {
			log.Printf("Failed to insert tag achievement %q: %v", ach.Name, err)
		}
	}
	log.Printf("Inserted %d 'tag_collector' achievements", len(tagAchievements))

	var achCount int
	_ = db.QueryRow(ctx, "SELECT COUNT(*) FROM tour_guide.achievements").Scan(&achCount)
	log.Printf("Total achievements in DB: %d", achCount)

	log.Println("\nDatabase seeding complete!")
}

func trim(s string, maxLen int) string {
	if len(s) > maxLen {
		return s[:maxLen]
	}
	return s
}

func getEnv(key, defaultValue string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return defaultValue
}
