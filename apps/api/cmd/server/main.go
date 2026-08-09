package main

import (
	"config"
	"database"
	"log"
)

func main() {
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatal(err)
	}

	_, err = database.NewPostgres(cfg.DatabaseUrl)
	if err != nil {
		log.Fatal(err)
	}

	log.Println("Connected to database")
}
