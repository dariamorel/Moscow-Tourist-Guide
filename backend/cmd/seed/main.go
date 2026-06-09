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
