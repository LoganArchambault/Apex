package com.example.apex.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 → 2 : planification, rappels et résultats du suivi GPS.
 * Les colonnes sont ajoutées à la table existante : aucune donnée n'est perdue.
 * Les séances existantes reçoivent « aucun rappel » et « aucun suivi » (null).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workouts ADD COLUMN datePrevue INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN rappelActif INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE workouts ADD COLUMN rappelEnvoye INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE workouts ADD COLUMN distanceMetres REAL")
        db.execSQL("ALTER TABLE workouts ADD COLUMN dureeSecondes INTEGER")
    }
}
