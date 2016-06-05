package com.mooc.upload_head_image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {
    private PopupWindow popupWindow;
    private ImageView iv_head;
    private TextView photograph, albums, cancel;
    private static final String ROOT_NAME = "HEAD_CACHE";
    private File mTempCameraFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        iv_head.setOnClickListener(this);
    }

    private void showPopupWindow() {
        if (popupWindow == null) {
            View view = View.inflate(this, R.layout.pop_select_photo, null);
            popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT, true);
            initPop(view);
        }
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow
                .setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(iv_head, Gravity.CENTER, 0, 0);

    }

    public void initPop(View view) {
        photograph = (TextView) view.findViewById(R.id.photograph);// 拍照
        albums = (TextView) view.findViewById(R.id.albums);// 相册
        cancel = (TextView) view.findViewById(R.id.cancel);// 取消
        photograph.setOnClickListener(this);
        albums.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_head:
                showPopupWindow();
                break;
            case R.id.photograph:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempCameraFile()));
                startActivityForResult(cameraIntent, 0);
                break;
            case R.id.albums:
                Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                startActivityForResult(albumIntent, 1);
                break;
            case R.id.cancel:
                popupWindow.dismiss();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 0:       // 照相机返回结果
                    startCrop(Uri.fromFile(getTempCameraFile()), 260, 260);
                    break;
                case 1:        // 相册返回结果
                    Uri uri = data.getData();
                    startCrop(uri, 260, 260);
                    break;
                case 2:         // 裁剪返回结果
                    sendImage(getTempCameraFile());
                    break;
            }
        }
    }
    private void sendImage(final File file) {
        if (file == null) {
            Toast.makeText(this, "找不到此图片", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
        iv_head.setImageBitmap(bitmap);
        deleteTemporary();
        // 可以在这里统一进行联网上传操作，把上传结果传回，也可以直接传file交给原页面处理

    }
    /**
     * 开始裁剪
     * 附加选项	   数据类型	    描述
     * crop	        String	    发送裁剪信号
     * aspectX	    int	        X方向上的比例
     * aspectY	    int	        Y方向上的比例
     * outputX	    int	        裁剪区的宽
     * outputY	    int	        裁剪区的高
     * scale	    boolean	    是否保留比例
     * return-data	boolean	    是否将数据保留在Bitmap中返回
     * data	        Parcelable	相应的Bitmap数据
     * circleCrop	String	    圆形裁剪区域？
     * MediaStore.EXTRA_OUTPUT ("output")	URI	将URI指向相应的file:///...
     *
     * @param uri uri
     */
    private void startCrop(Uri uri, int outputX, int outputY) {

        Intent intent = new Intent("com.android.camera.action.CROP"); //调用Android系统自带的一个图片剪裁页面
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");//进行修剪
        // aspectX aspectY 是宽高的比例
        if (outputX == outputY) {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        } else {
            intent.putExtra("scale", true);
        }
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempCameraFile()));
        startActivityForResult(intent, 2);
    }


private File getTempCameraFile() {
    if (mTempCameraFile == null)
        mTempCameraFile = getTempMediaFile();
    return mTempCameraFile;
}
    /**
     * 获取相机的file
     */
    public File getTempMediaFile() {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String fileName = Environment.getExternalStorageDirectory()
                    + File.separator + ROOT_NAME + File.separator+ "image" +
                    System.currentTimeMillis() + ".jpg";
            file = new File(fileName);
        }
        return file;
    }
    /**
     * 刪除临时保存的图片
     */
    private void deleteTemporary() {
        File file = getTempCameraFile();
        if (file.exists()) {
            deleteAll(file);
        }
    }

    /**
     * 递归删除文件
     *
     * @param file 要删除的文件或者文件夹
     * @throws IOException 文件找不到或者删除错误的时候抛出
     */
    public static void deleteAll(File file) {
        // 文件夹不存在不存在
        if (!file.exists()) {
            Log.d("rrx", "指定目录不存在:" + file.getName());
        }
        boolean rslt = true;// 保存中间结果
        if (!(rslt = file.delete())) {// 先尝试直接删除
            // 若文件夹非空。枚举、递归删除里面内容
            File subs[] = file.listFiles();
            for (int i = 0; i <= subs.length - 1; i++) {
                if (subs[i].isDirectory()) {
                    deleteAll(subs[i]);// 递归删除子文件夹内容
                }
                rslt = subs[i].delete();// 删除子文件夹本身
            }
            rslt = file.delete();// 删除此文件夹本身
        }
        if (!rslt) {
            Log.d("rrx", "无法删除:" + file.getName());
        }
        return;
    }

}
