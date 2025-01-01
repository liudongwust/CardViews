# CardViews
一款好用的android倒角（倒圆角，二阶贝塞尔倒角，三阶贝塞尔倒角）、边框、渐变UI基础组件，他是基于原生组件（FrameLayout、LinearLayout、ConstraintLayout、TextView、ImageView）扩展了更多功能，使用这些组件之后你不必再需要写那些烦人的drawable文件;组建的属性在清单文件中能直接被渲染，所见即所得。

## 用法
- 线性渐变 

    属性：card_linear_gradient   

    格式：orientation,colorsNum,#FFFFFF,#000000,#FF0000,positionNum,0,0.3,1

    这个格式设计灵感来自网络协议的数据包，用一个字符串来描述线性渐变

    orientation表示线性渐变的方向，可取值如下

        字符串类型：
        LEFT_RIGHT， LT_RB， TOP_BOTTOM， RT_LB， RIGHT_LEFT， RB_LT， BOTTOM_TOP， LB_RT
        数值类型（表示角度，0表示x轴正方向，角度增加方向为顺时针方向）：
        0,33.3,45,120...
    
    colorsNum表示渐变的颜色采样数

    positionNum表示渐变的颜色位置

    colorsNum必须和positionNum相等
    
    示例:
    ```
    <FrameLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <com.wustfly.cardviews.TextCard
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:card_linear_gradient="LEFT_RIGHT,2,#E09FA7,#7DCBD5,2,0,1" />

    </FrameLayout>
    ```
    ![img.png](pics/img.png)
    ```
    <FrameLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <com.wustfly.cardviews.TextCard
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:card_linear_gradient="12.5,3,#E09FA7,#66FFFF00,#7DCBD5,3,0,0.5,1" />

    </FrameLayout>
    ```
    ![img_1.png](pics/img_1.png)

- 固定高宽比View

    属性：card_dimension_ratio

    格式：2:1(表示宽高比为2：1)

    在使用这个属性时需注意

        1.须指定view的高度或宽度，另一个设置为0dp
        2.不建议在view的parent为ConstraintLayout中使用该属性，因为ConstraintLayout已有相关的高宽比表达

    示例：
    ```
    <FrameLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <com.wustfly.cardviews.TextCard
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_gravity="center"
            app:card_dimension_ratio="1:1"
            app:card_linear_gradient="12.5,3,#E09FA7,#66FFFF00,#7DCBD5,3,0,0.5,1" />

    </FrameLayout>
    ```
    ![img_2.png](pics/img_2.png)
    ```
    <FrameLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <com.wustfly.cardviews.TextCard
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_gravity="center"
            app:card_dimension_ratio="1:4"
            app:card_linear_gradient="12.5,3,#E09FA7,#66FFFF00,#7DCBD5,3,0,0.5,1" />

    </FrameLayout>
    ```
    ![img_3.png](pics/img_3.png)
    配合LinearLayout的layout_weight属性做比例布局
    ```
    <LinearLayout
        android:id="@+id/main"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        tools:context=".MainActivity">

        <com.wustfly.cardviews.TextCard
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="1"
            app:card_linear_gradient="LEFT_RIGHT,2,#E09FA7,#7DCBD5,2,0,1"
            app:card_dimension_ratio="1:1" />

        <com.wustfly.cardviews.TextCard
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="2"
            app:card_linear_gradient="LEFT_RIGHT,2,#E09FA7,#7DCBD5,2,0,1"
            app:card_dimension_ratio="1:1" />

        <com.wustfly.cardviews.TextCard
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="4"
            app:card_linear_gradient="LEFT_RIGHT,2,#E09FA7,#7DCBD5,2,0,1"
            app:card_dimension_ratio="1:1" />

    </LinearLayout>
    ```
    ![img_4.png](pics/img_4.png)
