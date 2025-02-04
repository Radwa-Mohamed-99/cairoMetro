import com.ramd.cairoMetro.pojo.Paths

data class StationsCount(private val path: Paths){
    fun stationCountOfShort(start:String,arrival:String): Int {
        val stationCount = path.findShortPath(start,arrival).size
        return stationCount
    }
    fun stationCountOfAllPaths(paths:MutableList<List<String>>,index:Int): Int {
        val stationCount = paths[index].count()
        return stationCount
    }
}