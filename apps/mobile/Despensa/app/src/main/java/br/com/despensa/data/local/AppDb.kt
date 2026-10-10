package br.com.despensa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import br.com.despensa.data.local.dao.BaixaDao
import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.data.local.entity.MotivoBaixa

class Converters {
    @TypeConverter fun fromLocal(v: LocalEstoque): String = v.name
    @TypeConverter fun toLocal(v: String): LocalEstoque =
        runCatching { LocalEstoque.valueOf(v) }.getOrDefault(LocalEstoque.DESPENSA)
    @TypeConverter fun fromMotivo(v: MotivoBaixa): String = v.name
    @TypeConverter fun toMotivo(v: String): MotivoBaixa =
        runCatching { MotivoBaixa.valueOf(v) }.getOrDefault(MotivoBaixa.CONSUMIDO)
}

@Database(entities = [EstoqueEntity::class, BaixaEntity::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun estoque(): EstoqueDao
    abstract fun baixa(): BaixaDao
}
