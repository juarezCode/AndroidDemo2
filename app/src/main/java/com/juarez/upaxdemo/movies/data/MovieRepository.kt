package com.juarez.upaxdemo.movies.data

import com.juarez.upaxdemo.utils.NetworkResponse
import com.juarez.upaxdemo.utils.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MovieRepository {
    fun getAllPopularMovies(): Flow<Resource<List<Movie>>>
    fun getAllTopRatedMovies(): Flow<Resource<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Resource<Movie>>
    val popularMovies: Flow<List<Movie>>
    val topRatedMovies: Flow<List<Movie>>
}

class MovieRepositoryImpl @Inject constructor(
    private val remoteDataSource: MovieRemoteDataSource,
    private val localDataSource: MovieLocalDataSource,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MovieRepository {

    override fun getAllPopularMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading(true))
        val total = localDataSource.getTotalPopularMovies()
        if (total < 1) {
            val response = withContext(defaultDispatcher) { remoteDataSource.getPopularMoviesAPI() }

            if (response is NetworkResponse.Success) {
                localDataSource.saveMovies(response.data!!.map { it.toEntity("popular") })
            } else emit(Resource.Error(response.message!!))
        }
        emit(Resource.Loading(false))
    }

    override fun getAllTopRatedMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading(true))
        val total = localDataSource.getTotalTopRatedMovies()
        if (total < 1) {
            val response = withContext(defaultDispatcher) { remoteDataSource.getTopRatedMovies() }

            if (response is NetworkResponse.Success) {
                localDataSource.saveMovies(response.data!!.map { it.toEntity("top") })
            } else emit(Resource.Error(response.message!!))
        }
        emit(Resource.Loading(false))
    }

    override fun getMovieDetail(movieId: Int): Flow<Resource<Movie>> = flow {
        emit(Resource.Loading(true))
        val movie = localDataSource.getMovieById(movieId)
        if (movie != null) {
            emit(Resource.Success(movie.toModel()))
        } else {
            val response = withContext(defaultDispatcher) {
                remoteDataSource.getMovieDetail(movieId)
            }

            if (response is NetworkResponse.Success) emit(Resource.Success(response.data!!))
            else emit(Resource.Error(response.message!!))
        }
        emit(Resource.Loading(false))
    }

    override val popularMovies: Flow<List<Movie>> =
        localDataSource.popularMovies.map { movieEntities ->
            movieEntities.map { movieEntity -> movieEntity.toModel() }
        }

    override val topRatedMovies: Flow<List<Movie>> =
        localDataSource.topRatedMovies.map { movieEntities ->
            movieEntities.map { movieEntity -> movieEntity.toModel() }
        }
}