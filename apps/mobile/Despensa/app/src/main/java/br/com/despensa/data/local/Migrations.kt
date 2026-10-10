package br.com.despensa.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migração v1 → v2 (Sprint 2, prepara tela R$ da Sprint 3).
 * Sem perda: ALTER adiciona precoRef (nullable) + CREATE TABLE baixa.
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE estoque ADD COLUMN precoRef REAL")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `baixa` (" +
                "`id` TEXT NOT NULL, `nome` TEXT NOT NULL, " +
                "`quantidade` REAL NOT NULL, `unidade` TEXT NOT NULL, " +
                "`valorTotal` REAL NOT NULL, `motivo` TEXT NOT NULL, " +
                "`ts` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}
