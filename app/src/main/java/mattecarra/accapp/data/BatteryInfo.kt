package mattecarra.accapp.data

class BatteryInfo(val status: String, val health: String, val current: Int, voltage: Int, val temp: Int) {
    val voltage: Float =
        if(voltage > 1000000)
            voltage / 1000000f
        else
            voltage / 1000f
}