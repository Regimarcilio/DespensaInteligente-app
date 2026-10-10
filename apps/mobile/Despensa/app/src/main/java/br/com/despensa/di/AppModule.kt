package br.com.despensa.di

import android.content.Context
import androidx.room.Room
import br.com.despensa.data.local.AppDb
import br.com.despensa.data.local.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDb =
        Room.databaseBuilder(ctx, AppDb::class.java, "despensa.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideEstoque(db: AppDb) = db.estoque()

    @Provides
    fun provideBaixa(db: AppDb) = db.baixa()
}
