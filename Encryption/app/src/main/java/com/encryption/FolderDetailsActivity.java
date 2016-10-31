package com.encryption;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.encryption.biz.AsyncHandleCallback;
import com.encryption.biz.DataProvider;
import com.encryption.biz.FolderBiz;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FolderDetailsActivity";

    private Context context;

    private TextView txt_count;

    private ListView lv_files;

    private FilesAdapter filesAdapter;

    private String folderPath;

    private Button btn_encryption;
    private Button btn_decryption;

    private int currPage = 1;
    private static final int PAGE_SIZE = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_details);
        context = this;

        txt_count = (TextView) findViewById(R.id.txt_count);

        btn_encryption = (Button) findViewById(R.id.btn_encryption);
        btn_encryption.setOnClickListener(this);
        btn_decryption = (Button) findViewById(R.id.btn_decryption);
        btn_decryption.setOnClickListener(this);

        lv_files = (ListView) findViewById(R.id.lv_files);
        filesAdapter = new FilesAdapter(context, null);
        lv_files.setAdapter(filesAdapter);

        lv_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = (String) filesAdapter.getItem(position);

                Intent intent = new Intent(context, BigViewActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });

        lv_files.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 判断滚动到底部
                if (lv_files.getLastVisiblePosition() == (lv_files.getCount() - 1)) {
                    if (currPage > DataProvider.getPageCount(folderPath, PAGE_SIZE)) {
                        return;
                    }

                    loadFileList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        // 获取上个界面传递的参数
        boolean isHasParams = getIntentParams();
        if (!isHasParams) {
            Toast.makeText(this, "没有传递文件夹地址", Toast.LENGTH_LONG).show();
            return;
        }

        loadFileList();

    }

    /**
     * 加载图片列表
     */
    private void loadFileList() {
        // 获取文件夹下的文件
        getFileList(folderPath, callback);
    }

    /**
     * 获取图片列表
     * @param path
     * @param callback
     */
    public void getFileList(final String path, final AsyncHandleCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> fileList = DataProvider.getFileByPage(path, currPage, PAGE_SIZE);

                if (callback != null) {
                    callback.onSucc(fileList);
                }
            }
        }).start();
    }

    /** 获取数据回调对象 */
    final private AsyncHandleCallback callback = new AsyncHandleCallback() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSucc(final Object obj) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (obj == null) {
                        Toast.makeText(context, "文件夹为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currPage += 1;

                    // 清空选择内容
                    filesAdapter.setCheckedEmpty();

                    // 设置列表数据
                    List<String> fileList = (List<String>) obj;
                    filesAdapter.addFileList(fileList);

                    // 设置个数
                    txt_count.setText("共有文件个数" + filesAdapter.getCount() + "个");

                }
            });
        }

        @Override
        public void onErr(int errCode, String errMsg) {
        }

        @Override
        public void onFinish(Object obj) {
        }
    };

    /**
     * 获取传递的参数
     *
     * @return
     */
    public boolean getIntentParams() {
        Intent intent = getIntent();

        Bundle params = intent.getExtras();
        folderPath = params.getString("folder_path");
        Log.i(TAG, "getIntentParams() 收到的参数=" + folderPath);

        return !TextUtils.isEmpty(folderPath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_decryption:
                decryptionFile();
                break;
            case R.id.btn_encryption:
                encryptionFile();
                break;
        }
    }

    /**
     * 加密选中的文件
     */
    private void encryptionFile() {
        // 获取选中的文件
        List<String> checkedList = filesAdapter.getCheckedList();

        if (checkedList == null || checkedList.size() == 0) {
            Toast.makeText(this, "没有选中任何文件", Toast.LENGTH_SHORT).show();
            return;
        }

        int total = checkedList.size();

        int count = 0;
        for (int i = 0; i < checkedList.size(); i++) {
            String path = checkedList.get(i);

            boolean isSucc = FolderBiz.encryptionFile(new File(path));
            if (isSucc) {
                count += 1;
            }
        }

        if (count == total) {
            Toast.makeText(this, "对选中的文件加密完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "有" + (total - count) + "个文件加密失败", Toast.LENGTH_SHORT).show();
        }

        loadFileList();

    }

    private void decryptionFile() {
        // 获取选中的文件
        List<String> checkedList = filesAdapter.getCheckedList();

        if (checkedList == null || checkedList.size() == 0) {
            Toast.makeText(this, "没有选中任何文件", Toast.LENGTH_SHORT).show();
            return;
        }

        int total = checkedList.size();

        int count = 0;
        for (int i = 0; i < checkedList.size(); i++) {
            String path = checkedList.get(i);

            boolean isSucc = FolderBiz.decryptionFile(new File(path));
            if (isSucc) {
                count += 1;
            }
        }

        if (count == total) {
            Toast.makeText(this, "对选中的文件解密完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "有" + (total - count) + "解密失败", Toast.LENGTH_SHORT).show();
        }


        loadFileList();
    }


    /**
     * 文件列表适配器
     */
    class FilesAdapter extends BaseAdapter {

        private Context context;

        private List<String> fileList;

        private LayoutInflater inflater;

        private List<String> checkedList;


        /**
         * 构造方法
         *
         * @param context
         * @param fileList
         */
        public FilesAdapter(Context context, List<String> fileList) {
            this.context = context;

            if(fileList == null) {
                this.fileList = new ArrayList<String>();
            } else {
                this.fileList = fileList;
            }

            this.inflater = LayoutInflater.from(this.context);
            this.checkedList = new ArrayList<String>();
        }

        /**
         * 设置数据
         *
         * @param fileList
         */
        public void setFileList(List<String> fileList) {
            this.fileList.clear();
            this.fileList = null;

            this.fileList = fileList;

            refresh();
        }

        /**
         * 添加数据
         * @param fileList
         */
        public void addFileList(List<String> fileList) {
            if(fileList == null || fileList.size() == 0) {
                return ;
            }

            this.fileList.addAll(fileList);

            refresh();
        }

        /**
         * 刷新
         */
        public void refresh() {
            notifyDataSetChanged();
        }

        /**
         * 清空选项
         */
        public void setCheckedEmpty() {
            if(checkedList!=null) {
                checkedList.clear();
            }
        }

        @Override
        public int getCount() {
            if (fileList == null) {
                return 0;
            }

            return fileList.size();
        }

        @Override
        public Object getItem(int position) {
            if (fileList == null) {
                return null;
            }

            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.item_list_files, null);

                holder = new ViewHolder();

                holder.txt_index = (TextView) view.findViewById(R.id.txt_index);
                holder.img_icon = (SimpleDraweeView) view.findViewById(R.id.img_icon);
                holder.txt_name = (TextView) view.findViewById(R.id.txt_name);
                holder.cb_file = (CheckBox) view.findViewById(R.id.cb_file);

                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            final String path = fileList.get(position);

            // 索引
            holder.txt_index.setText((position + 1) + ".");
            // 图片
            if (!FolderBiz.isEncryptionFile(path)) {
                holder.img_icon.setImageURI(Uri.parse("file://" + path));
            } else {
                holder.img_icon.setImageURI(Uri.parse("res://com.encryption/" + R.drawable.a));
            }

            // 名称
            String name = DataProvider.getFileName(path);
            holder.txt_name.setText(name);

            if(checkedList.contains(path)) {
                holder.cb_file.setChecked(true);
            } else {
                holder.cb_file.setChecked(false);
            }

            // 复选框单击事件
            holder.cb_file.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 选中
                    if (isChecked) {
                        // 不已经存在，加入
                        if (!checkedList.contains(path)) {
                            checkedList.add(path);
                        }
                    } else {
                        // 已经存在，移除
                        if (checkedList.contains(path)) {
                            checkedList.remove(path);
                        }
                    }
                }
            });

            return view;
        }

        public List<String> getCheckedList() {
            return checkedList;
        }

        class ViewHolder {
            TextView txt_index;

            SimpleDraweeView img_icon;

            TextView txt_name;

            CheckBox cb_file;
        }
    }
}
