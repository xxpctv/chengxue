# q215613905-Tbox
复刻自TG群俊于phper的库：https://github.com/q215613905/TVBoxOS/ 


1、修改软件名称地址
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/res/values/strings.xml

2、修改版本号
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/build.gradle

3、修改图标、背景、载入动画
背景
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable/app_bg.png
图标
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable/app_banner.png
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable-xxxhdpi/app_icon.png
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable-xxhdpi/app_icon.png
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable-xhdpi/app_icon.png
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable-hdpi/app_icon.png
载入动画
https://github.com/chengxue2020/q215613905-Tbox/tree/main/app/src/main/res/drawable/icon_loading.png

4、修改内置源
俊老仓库打开下面,第83行
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java
takagen99大佬仓库 改这里;app/src/main/res/values-zh/strings.xml


5、修改默认缩略图、硬解、dns
地址：
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/java/com/github/tvbox/osc/base/App.java

    private void initParams() {
        // 自定义默认配置
        Hawk.init(this).build();
        Hawk.put(HawkConfig.DEBUG_OPEN, false);
		Hawk.put(HawkConfig.HOME_REC, 1);       // Home Rec 0=豆瓣, 1=站点推荐, 2=历史
		Hawk.put(HawkConfig.SEARCH_VIEW, 1);    // 0 文字搜索列表 1 缩略图搜索列表
		Hawk.put(HawkConfig.IJK_CODEC, "硬解码");  //解码
		Hawk.put(HawkConfig.DOH_URL, 2);		//DOH
		Hawk.put(HawkConfig.SEARCH_VIEW, 1);

        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1);
			
        }
    }


6、修改直播界面频道列表中跳动条的颜色、个数
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/java/com/github/tvbox/osc/ui/tv/widget/AudioWaveView.java


7、修改点播详情界面字体的颜色（27行）
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/java/com/github/tvbox/osc/ui/adapter/SeriesAdapter.java


8、修改DOH
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/java/com/github/tvbox/osc/util/OkGoHelper.java


9、修改点播界面进度条颜色
https://github.com/chengxue2020/q215613905-Tbox/blob/main/app/src/main/res/drawable/shape_player_control_vod_seek.xml
