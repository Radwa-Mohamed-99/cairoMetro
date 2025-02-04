package com.ramd.cairoMetro.pojo

class Paths(private val metroLines: MetroLines){
    fun findAllPaths(start: String, end: String): List<List<String>> {
        val visited = mutableSetOf<String>()
        val path = mutableListOf<String>()
        val result = mutableListOf<List<String>>()
        dfs(start, end, visited, path, result)
        return result
    }
    fun findShortPath(start: String,end: String): List<String> {
        var shortPath = listOf<String>()
        if (findAllPaths(start,end).isNotEmpty()){
            shortPath = findAllPaths(start,end).minBy{it.size}
        }
         return shortPath
    }
    fun findShortPathLessInterSection(start: String,end: String): List<String> {
        var shortPath = listOf<String>()
        if (findAllPaths(start, end).isNotEmpty()){
            shortPath = findAllPaths(start, end).minBy { it.size + it.count {station -> station in metroLines.intersectionStations}}
        }
        return shortPath
    }
    private fun dfs(
        current: String,
        end: String,
        visited: MutableSet<String>,
        path: MutableList<String>,
        result: MutableList<List<String>>
    ){
        visited.add(current)
        path.add(current)

        if (current == end) {
            result.add(ArrayList(path))
        }
        else {
            metroLines.graph[current]?.forEach{ neighbor ->
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, end, visited, path, result)
                }
            }
        }

        visited.remove(current)
        path.removeAt(path.size - 1)
    }
}