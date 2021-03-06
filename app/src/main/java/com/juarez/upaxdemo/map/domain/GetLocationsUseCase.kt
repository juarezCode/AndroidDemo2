package com.juarez.upaxdemo.map.domain

import com.juarez.upaxdemo.map.data.Location
import com.juarez.upaxdemo.map.data.LocationsRepository
import com.juarez.upaxdemo.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationsUseCase @Inject constructor(private val repository: LocationsRepository) {
    operator fun invoke(): Flow<Resource<List<Location>>> = repository.getLocations()
}