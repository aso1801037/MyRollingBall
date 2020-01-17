package com.example.myrollingball

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import android.util.DisplayMetrics











class MainActivity : AppCompatActivity(), SensorEventListener,SurfaceHolder.Callback  {
    // プロパティ
    private var surfaceWidth:Int = 0; // サーフェスの幅
    private var surfaceHeight:Int = 0; // サーフェスの高さ
    private val radius = 50.0f; // ボールの半径
    private val coef = 1000.0f; // ボールの移動量を計算するための係数
    private var ballX:Float = 0f; // ボールの現在のX座標
    private var ballY:Float = 0f; // ボールの現在のY座標
    private var vx:Float = 0f; // ボールのX方向の加速度
    private var vy:Float = 0f; // ボールのY方向の加速度
    private var time:Long = 0L; // 前回の取得時間

    private var paint: Paint? = null
    private var path: Path? = null
    private val StrokeWidth1 = 20f
    private val StrokeWidth2 = 40f
    private var dp: Float = 0.toFloat()
    private var xc:Float = 0f;
    private var yc:Float = 0f;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder = surfaceView.holder; // サーフェスホルダーを取得
        // サーフェスホルダーのコールバックに自クラスを追加
        holder.addCallback(this);

        paint = Paint();
        path = Path();
        // スクリーンサイズからdipのようなものを作る
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        dp = resources.displayMetrics.density
        Log.d("debug", "fdp=$dp")

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //今回は何もしない
    }
    //センサーの値
    override fun onSensorChanged(event: SensorEvent?) {
        // イベントが何もなかったらそのままリターン
        if(event == null){ return; }

        // ボールの描画の計算処理
        if(time==0L){ time = System.currentTimeMillis();} // 最初の現在時刻を設定
        // イベントのセンサー種別が（加速度センサー）の時
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // センサーのx(左右),y（縦）値を取得
            val x = event.values[0] * -1;
            val y = event.values[1];
            // 経過時間を計算(今の時間-前の時間 = 経過時間)
            var t = (System.currentTimeMillis() - time).toFloat();
            // 今の時間を「前の時間」として保存
            time = System.currentTimeMillis();
            t /= 1000.0f;

            // 移動距離を計算（ボールをどれくらい動かすか）
            val dx = (vx*t) + (x*t*t)/2.0f; // xの移動距離(メートル)
            val dy = (vy*t) + (y*t*t)/2.0f; // yの移動距離（メートル）
            // メートルをピクセルのcmに補正してボールのX座標に足しこむ=新しいボールのX座標
            ballX += (dx*coef);
            // メートルをピクセルのcmに補正してボールのY座標に足しこむ=新しいボールのY座標
            ballY += (dy*coef);
            // 今の各方向の加速度を更新
            vx +=(x*t);
            vy +=(y*t);

            // 画面の端にきたら跳ね返る処理
            // 左右について
            if( (ballX -radius)<0 && vx<0 ){
                // 左にぶつかった時
                vx = -vx /1.5f;
                this.ballX = this.radius;
            }else if( (ballX+radius)>this.surfaceWidth && vx>0){
                // 右にぶつかった時
                vx = -vx/1.5f;
                this.ballX = (this.surfaceWidth-this.radius);
            }
            // 上下について
            if( (this.ballY -this.radius)<0 && vy<0 ){
                // 下にぶつかった時
                vy = -vy /1.5f;
                this.ballY = this.radius;
            }else if( (this.ballY+this.radius)>this.surfaceHeight && vy>0 ){
                // 上にぶつかった時
                vy = -vy/1.5f;
                this.ballY = this.surfaceHeight -this.radius;
            }


            if( (ballX -radius)<0 && vx<0 ){
                // 左にぶつかった時
                vx = -vx /1.5f;
                this.ballX = this.radius;
            }else if( (ballX+radius)>this.surfaceWidth && vx>0){
                // 右にぶつかった時
                vx = -vx/1.5f;
                this.ballX = (this.surfaceWidth-this.radius);
            }
            if( (this.ballY -this.radius)<this.xc - 40*dp &&(this.ballY -this.radius)>this.yc - 10*dp && vy<0 ){
                // 下にぶつかった時
                vy = -vy/1.5f;
            }else if( (this.ballX+this.radius)>this.xc + 150*dp&&(this.ballY+this.radius)>this.yc + 20*dp && vy>0 ){
                // 上にぶつかった時
                vy = -vy/1.5f;

            }


            // キャンバスに描画
            this.drawCanvas();




        }


    }

    // サーフェスが更新された時のイベント
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // サーフェスの幅と高さを設定
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        // ボールの初期位置を保存しておく
        this.ballX = (this.surfaceWidth/2).toFloat();
        this.ballY = (this.surfaceHeight/10).toFloat();

    }

    // サーフェスが破棄された時のイベント
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // センサーマネージャを取得
        val sensorManager =
            this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;

        // センサーマネージャに登録したリスナーを解除（自分自身を解除）
        sensorManager.unregisterListener(this);
    }

    // サーフェスが作成された時のイベント
    override fun surfaceCreated(holder: SurfaceHolder?) {
        // センサーマネージャをOSから取得
        val sensorManager =this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        // 加速度センサー(Accelerometer)を指定してセンサーマネージャからセンサーを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // リスナー登録して加速度センサーの監視を開始
        sensorManager.registerListener(
            this,// イベントリスナー機能をもつインスタンス（自クラスのインスタンス）
            accSensor,// 監視するセンサー（加速度センサー）
            SensorManager.SENSOR_DELAY_GAME // センサーの更新頻度
        )
    }
    // サーフェスのキャンバスに描画するメソッド
    private fun drawCanvas() {
        // キャンバスをロックして取得
        val canvas = surfaceView.holder.lockCanvas();
        // キャンバスの背景色を設定
        canvas.drawColor(Color.GREEN);
        // キャンバスに円を描いてボールにする
        canvas.drawCircle(
            this.ballX, // ボール中心のX座標
            this.ballY, // ボール中心のY座標
            this.radius, // 半径
            Paint().apply {
                this.color = Color.BLUE;
            } // ペイントブラシのインスタンス
        );

        val xc = canvas.width / 2
        val yc = canvas.height / 3
        Paint().setStyle(Paint.Style.STROKE);
        // (x1,y1,x2,y2,paint) 左上の座標(x1,y1), 右下の座標(x2,y2)
        canvas.drawRect(xc - 40*dp, yc - 10*dp,
            xc + 150*dp, yc + 20*dp, Paint()
        );

   /*     val xa = canvas.width / 7
        val ya = canvas.height / 2
        Paint().setStyle(Paint.Style.STROKE);
        // (x1,y1,x2,y2,paint) 左上の座標(x1,y1), 右下の座標(x2,y2)
        canvas.drawRect(xa - 40*dp, ya - 10*dp,
            xa + 10*dp, ya + 20*dp, Paint()
        );

        Paint().setStrokeWidth(StrokeWidth2);
        Paint().setAntiAlias(true);
        Paint().setStyle(Paint.Style.STROKE);
        // (x,y,r,paint) x座標, y座標, r半径
        canvas.drawCircle(200*dp, 300*dp, 30*dp, Paint()
        );

        Paint().setStrokeWidth(StrokeWidth2);
        Paint().setAntiAlias(true);
        Paint().setStyle(Paint.Style.STROKE);
        // (x,y,r,paint) x座標, y座標, r半径
        canvas.drawCircle(90*dp, 400*dp, 40*dp, Paint()
        );

        val xb = canvas.width
        val yb = canvas.height
        Paint().setStyle(Paint.Style.STROKE);
        // (x1,y1,x2,y2,paint) 左上の座標(x1,y1), 右下の座標(x2,y2)
        canvas.drawRect(xb - 40*dp, yb - 5*dp,
            xb + 10*dp, yb + 20*dp, Paint()
        );

    */
        // キャンバスをロック解除してキャンバスを描画(表示)
        surfaceView.holder.unlockCanvasAndPost(canvas);
    }



}
