package com.java.liangcong.news;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.java.liangcong.web.TencentNewsXmlParser;
import com.liangcong.news.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.java.liangcong.database.NewsDbSchema.NewsDbSchema;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class DisplayNewsActivity extends AppCompatActivity {
    private static final String APP_CACHE_DIRNAME = "/webcache";
    private String url;
    private WebView webView;
    private String newsTitle;
    final private int SUCCESSCODE = 1;
    Menu readMenu;

    File dir;

    private static final int REQUEST_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_news);

        File sdCard = Environment.getExternalStorageDirectory();
        dir = new File (sdCard.getAbsolutePath() + "/news_photo");

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        url = intent.getStringExtra("NEWS_URL");

        webView = (WebView) findViewById(R.id.web_view);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            //标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                newsTitle = title;
                ActionBar actionbar = getSupportActionBar();
                actionbar.setTitle(title);
            }
        });

        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT,无网络时，使用LOAD_CACHE_ELSE_NETWORK

        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        webView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()
                 + APP_CACHE_DIRNAME;
        //String cacheDirPath = getCacheDir()
        //        + APP_CACHE_DIRNAME;
        // 设置数据库缓存路径
        webView.getSettings().setDatabasePath(cacheDirPath);
        webView.getSettings().setAppCachePath(cacheDirPath);
        // 开启Application Cache功能
        webView.getSettings().setAppCacheEnabled(true);

        webView.loadUrl(url);

        //app内打开
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        readMenu = menu;
        if(getCollectedItem(url)!=null){//已收藏
            readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_24px);
        }else {
            readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_border_24px);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                //返回
                Intent intent=new Intent();
                setResult(30,intent);
                finish();
                return true;
            }
            case R.id.add_news: {
                //标记收藏
                if(getCollectedItem(url) == null) {
                    Toast.makeText(getApplicationContext(), "已收藏，请到首页收藏列表查看",
                            Toast.LENGTH_SHORT).show();
                    //先获取item
                    TencentNewsXmlParser.NewsItem newsItem = getItem(url);
                    //再写item到收藏db
                    ContentValues values = MainActivity.getContentValues(newsItem);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME1, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                    readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_24px);
                }else{
                    Toast.makeText(getApplicationContext(), "已移出收藏",
                            Toast.LENGTH_SHORT).show();
                    //删除
                    TencentNewsXmlParser.NewsItem newsItem = getCollectedItem(url);
                    ContentValues values = MainActivity.getContentValues(newsItem);
                    MainActivity.database.delete(NewsDbSchema.Newstable.NAME1, NewsDbSchema.Newstable.Cols.LINK +
                            " = ? ", new String[] {newsItem.link});
                    readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_border_24px);
                }
                return true;
            }
            case R.id.share:{
                //分享
                //权限申请，成功后分享
                permissiongen();
                return true;
            }
            case R.id.textshare:{
                //文字分享
                TencentNewsXmlParser.NewsItem itemToShare = getItem(url);
                shareText("文字分享", itemToShare.link,
                        "【" + itemToShare.type + "】" + Html.fromHtml(itemToShare.title).toString() +
                                "【" + itemToShare.link + "】" + Html.fromHtml(itemToShare.description).toString() + "......");
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public static TencentNewsXmlParser.NewsItem getItem(String link){
        TencentNewsXmlParser.NewsItem item = new TencentNewsXmlParser.NewsItem();
        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                item = cursor.getNewsItem();
                if ( item.link.equals(link)) return item;
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return null;
    }

    private static NewsCursorWrapper queryNews(String whereClause, String[] whereArgs){
        Cursor cursor = MainActivity.database.query(
                NewsDbSchema.Newstable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                NewsDbSchema.Newstable.Cols.PUBDATE+" DESC"
        );
        return new NewsCursorWrapper(cursor);
    }

    public static TencentNewsXmlParser.NewsItem getCollectedItem(String link){
        TencentNewsXmlParser.NewsItem item = null;
        NewsCursorWrapper cursor = queryCollectedNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                item = cursor.getNewsItem();
                if ( item.link.equals(link)) return item;
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return null;
    }

    private static NewsCursorWrapper queryCollectedNews(String whereClause, String[] whereArgs){
        Cursor cursor = MainActivity.database.query(
                NewsDbSchema.Newstable.NAME1,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                NewsDbSchema.Newstable.Cols.PUBDATE+" DESC"
        );
        return new NewsCursorWrapper(cursor);
    }

    public Bitmap shotActivityNoBar(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    public File saveBitmapToFile(Bitmap bitmap) {

        dir.mkdir();
        File f = new File(dir, "NEWS_SHARE.jpeg");
        try {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void shareImg(String dlgTitle, String subject, String content,
                          Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }

    private void permissiongen() {
        //处理需要动态申请的权限
        PermissionGen.with(this)
                .addRequestCode(SUCCESSCODE)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request();
    }

    //申请权限结果的返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //权限申请成功
    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        //在这个方法中做一些权限申请成功的事情
        File f = saveBitmapToFile(shotActivityNoBar(this));//储存
        TencentNewsXmlParser.NewsItem itemToShare = getItem(url);
        shareImg("图片分享", itemToShare.link,
                "【" + itemToShare.type + "】" + Html.fromHtml(itemToShare.title).toString() +
                        "【" + itemToShare.link + "】" + Html.fromHtml(itemToShare.description).toString() + "......",
                getImageContentUri(this, f));
    }
    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        showNormalDialog();
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setIcon(R.drawable.ic_baseline_build_24px);
        normalDialog.setTitle("需要读写文件权限");
        normalDialog.setMessage("为了分享图片新闻，News需要获取当前新闻的截图。请到系统设置给予News文件读写权限，以使用图片分享功能。否则，您将只能分享无图片新闻。");
        normalDialog.setPositiveButton("好的",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        normalDialog.setNegativeButton("分享无图新闻",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TencentNewsXmlParser.NewsItem itemToShare = getItem(url);
                        shareText("文字分享", itemToShare.link,
                                "【" + itemToShare.type + "】" + Html.fromHtml(itemToShare.title).toString() +
                                        "【" + itemToShare.link + "】" + Html.fromHtml(itemToShare.description).toString() + "......");
                    }
                });

        // 显示
        normalDialog.show();
    }

    private void shareText(String dlgTitle, String subject, String content) {
        if (content == null || "".equals(content)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        intent.putExtra(Intent.EXTRA_TEXT, content);

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
}

