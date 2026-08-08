package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.StandingBaseline
import kotlinx.coroutines.flow.Flow

interface StandingBaselineRepository {

    fun getBaselines(): Flow<List<StandingBaseline>>
}