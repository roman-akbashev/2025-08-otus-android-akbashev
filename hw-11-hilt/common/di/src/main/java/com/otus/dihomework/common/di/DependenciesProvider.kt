package com.otus.dihomework.common.di

interface Dependencies

interface DependenciesProvider {
    fun getDependencies(): Dependencies
}

inline fun <reified T : Dependencies> DependenciesProvider.findDependencies(): T {
    return getDependencies() as T
}

inline fun <reified T : Dependencies> android.content.Context.findDependencies(): T {
    return (applicationContext as DependenciesProvider).findDependencies()
}