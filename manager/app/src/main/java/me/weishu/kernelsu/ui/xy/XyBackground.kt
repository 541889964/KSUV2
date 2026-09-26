package me.weishu.kernelsu.ui.xy
import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
class XyTilt(c:Context):SensorEventListener {
    private val sm=c.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val g=sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
    var tx by mutableFloatStateOf(0f)
    private var last=0f; private var reg=false
    fun start(){ if(reg||g==null) return; sm.registerListener(this,g,SensorManager.SENSOR_DELAY_GAME); reg=true }
    fun stop(){ if(reg){ sm.unregisterListener(this); reg=false } }
    override fun onSensorChanged(e:SensorEvent){
        if(e.sensor.type!=Sensor.TYPE_GRAVITY) return
        val rx=e.values[0]/9.81f; last+=(rx-last)*0.35f; tx=last.coerceIn(-1f,1f)
    }
    override fun onAccuracyChanged(s:Sensor?,a:Int){}
}
@Composable fun rememberXyTilt():XyTilt? {
    val c=LocalContext.current; val t=remember(c){ XyTilt(c) }
    DisposableEffect(t){ t.start(); onDispose{ t.stop() } }
    return t
}
@Composable private fun loadImgs(dir:String):List<ImageBitmap>{
    val c=LocalContext.current
    val list by produceState<List<ImageBitmap>>(emptyList(),c,dir){
        value=withContext(Dispatchers.IO){
            try{ (c.assets.list(dir)?:emptyArray()).mapNotNull{ n->
                try{ c.assets.open("$dir/$n").use{ BitmapFactory.decodeStream(it)?.asImageBitmap() } }catch(_:Exception){null}
            } }catch(_:Exception){ emptyList() }
        }
    }
    return list
}
@Composable fun XyWallpaperLayer(){
    val imgs=loadImgs("xy_wall"); val cfg=XyPrefs.config
    if(imgs.isEmpty()||!cfg.wallpaper) return
    val idx=remember(imgs){ Random.nextInt(imgs.size) }
    Box(Modifier.fillMaxSize()){
        Image(imgs[idx%imgs.size],null,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
        if(cfg.backgroundDim>0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=cfg.backgroundDim)))
    }
}
private class Pr(var x:Float,var y:Float,val size:Float,val fall:Float,
                 var rot:Float,val rs:Float,var ph:Float,val a:Float,val idx:Int)
@Composable fun XyParticleLayer(type:XyParticle){
    if(type==XyParticle.NONE) return
    val imgs=loadImgs("xy_snow"); val tilt=rememberXyTilt(); val cfg=XyPrefs.config
    val n=cfg.particleCount.coerceIn(10,200)
    var tick by remember{ mutableIntStateOf(0) }
    val ps=remember(n,type){ List(n){ Pr(
        Random.nextFloat(),Random.nextFloat(),
        Random.nextFloat()*24f+16f, Random.nextFloat()*0.06f+0.03f,
        Random.nextFloat()*6.28f, (Random.nextFloat()-0.5f)*1.6f,
        Random.nextFloat()*6.28f, Random.nextFloat()*0.35f+0.55f,
        Random.nextInt(999)) } }
    LaunchedEffect(Unit){
        var last=0L
        while(true){ withFrameNanos{ now->
            if(last==0L){ last=now; return@withFrameNanos }
            var dt=(now-last)/1_000_000_000f; last=now
            if(dt>0.1f) dt=0.1f
            val tx=tilt?.tx?:0f; val wind=tx*1.4f*cfg.tiltStrength
            for(p in ps){
                p.y+=p.fall*dt; p.x+=wind*0.16f*dt
                p.rot+=p.rs*dt; p.ph+=dt
                if(p.y>1.1f){ p.y=-0.08f; p.x=Random.nextFloat() }
                if(p.x>1.08f) p.x=-0.08f
                if(p.x<-0.08f) p.x=1.08f
            }
            tick++
        } }
    }
    if(imgs.isEmpty()) return
    Canvas(Modifier.fillMaxSize()){ tick
        val w=size.width; val h=size.height
        for(p in ps){
            val px=p.x*w; val py=p.y*h
            val a=(p.a*(kotlin.math.sin(tick*0.03f+p.ph)*0.2f+0.8f)).coerceIn(0f,1f)
            val img=imgs[p.idx%imgs.size]; val ds=p.size.toInt()
            rotate(p.rot*180f/Math.PI.toFloat(), Offset(px,py)){
                drawImage(img,alpha=a,
                    dstOffset=IntOffset((px-ds/2f).toInt(),(py-ds/2f).toInt()),
                    dstSize=IntSize(ds,ds))
            }
        }
    }
}
