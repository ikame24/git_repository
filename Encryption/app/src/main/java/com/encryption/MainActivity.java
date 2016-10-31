package com.encryption;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.encryption.biz.DataProvider;
import com.encryption.biz.FolderBiz;
import com.encryption.biz.FolderInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Context context;

    private ListView lv_folders;

    private FolderAdapter folderAdapter;

    // 加密按钮
    private Button btn_encryption;
    // 解密按钮
    private Button btn_decryption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        lv_folders = (ListView) findViewById(R.id.lv_folders);

        // 获取数据
        List<FolderInfo> folderList = DataProvider.getFolders();

        // 生成适配器，显示内容
        folderAdapter = new FolderAdapter(this, folderList);
        lv_folders.setAdapter(folderAdapter);

        btn_encryption = (Button) findViewById(R.id.btn_encryption);
        btn_encryption.setOnClickListener(this);
        btn_decryption = (Button) findViewById(R.id.btn_decryption);
        btn_decryption.setOnClickListener(this);

        lv_folders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FolderInfo folderInfo = (FolderInfo) folderAdapter.getItem(position);
                enterFolderDetailView(folderInfo.getPath());
            }
        });

    }

    /**
     * 进入文件夹详情界面
     * @param folderPath
     */
    private void enterFolderDetailView(String folderPath) {
        Intent intent = new Intent(this, FolderDetailsActivity.class);

        Bundle params = new Bundle();
        params.putString("folder_path", folderPath);

        intent.putExtras(params);
        startActivity(intent);

        Log.i(TAG, "----------->进入详情");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_encryption:
                encryption();
                break;

            case R.id.btn_decryption:
                decryption();
                break;
        }
    }

    /**
     * 解密文件
     */
    private void decryption() {
        // 选中的加密目录
        List<String> deFolder = folderAdapter.getCheckedList();
        if (deFolder == null || deFolder.size() == 0) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "没有选中要解密的文件", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // 选中第一个加密
        String decodeFolder = deFolder.get(0);
        FolderBiz.decodeFolder(decodeFolder, new DataProvider.HandleCallback() {
            @Override
            public void onStart() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "开始解密选中的目录。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int curr, int total) {
                Log.i(TAG, "当前解密" + curr + "/" + total);
            }

            @Override
            public void onFinish() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        folderAdapter.refresh();

                        Toast.makeText(context, "解密完成。", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 加密文件
     */
    private void encryption() {
        // 选中的加密目录
        final List<String> folderList = folderAdapter.getCheckedList();
        if (folderList == null || folderList.size() == 0) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "没有选中要加密的文件", Toast.LENGTH_SHORT).show();
                }
            });

            return;
        }

        Log.i(TAG, "---> 选中加密文件夹=" + folderList.get(0));

        // 选中第一个加密
        String name = folderList.get(0);
        FolderBiz.encodeFolder(name, new DataProvider.HandleCallback() {
            @Override
            public void onStart() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "开始加密选中的目录。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int curr, int total) {
                Log.i(TAG, "当前加密" + curr + "/" + total);
            }

            @Override
            public void onFinish() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        folderAdapter.refresh();

                        Toast.makeText(context, "加密完成。", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(folderAdapter != null) {
            folderAdapter.refresh();
        }
    }

    /**
     * 文件夹列表适配器
     */
    class FolderAdapter extends BaseAdapter {
        private Context context;

        private List<FolderInfo> folderList;

        private LayoutInflater inflater;

        private List<String> checkedList;


        /**
         * 构造方法
         *
         * @param context
         * @param folderList
         */
        public FolderAdapter(Context context, List<FolderInfo> folderList) {
            this.folderList = folderList;
            this.context = context;

            inflater = LayoutInflater.from(this.context);
            checkedList = new ArrayList<String>();
        }

        public void refresh() {
            if(checkedList != null) {
                checkedList.clear();
            }

            notifyDataSetChanged();
        }

        public List<String> getCheckedList() {
            return checkedList;
        }

        @Override
        public int getCount() {
            if (folderList == null) {
                return 0;
            }

            return folderList.size();
        }

        @Override
        public Object getItem(int position) {
            if (folderList == null) {
                return null;
            }

            return folderList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            ViewHolder holder = null;

            if (view == null) {
                view = inflater.inflate(R.layout.item_list_folder, null);

                holder = new ViewHolder();
                holder.txt_index = (TextView) view.findViewById(R.id.txt_index);
                holder.txt_name = (TextView) view.findViewById(R.id.txt_name);
                holder.cb_folder = (CheckBox) view.findViewById(R.id.cb_folder);
                holder.layout_folder = (RelativeLayout) view.findViewById(R.id.layout_folder);

                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            final FolderInfo folder = folderList.get(position);

            // 根据加密结果获取的颜色值
            int encryptionColor = getColorByEncryptionType(folder);

            // 索引
            holder.txt_index.setText((position + 1) + ".");
            holder.txt_index.setTextColor(encryptionColor);
            // 名称
            holder.txt_name.setText(folder.getName());
            // 设置字体颜色
            holder.txt_name.setTextColor(encryptionColor);

            // 设置复选框的选择状态
            holder.cb_folder.setChecked(checkedList.contains(folder.getName()));

            // 单击选中
            holder.cb_folder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setChecked(folder, isChecked);
                }
            });

            return view;
        }

        /**
         * 根据加密类型返回颜色值
         *
         * @param folder
         * @return
         */
        private int getColorByEncryptionType(FolderInfo folder) {
            DataProvider.EncryptionType encryptionType = FolderBiz.isEncryption(folder.getName());
            Log.i(TAG, "------->name=" + folder.getName() + ", isEncryption=" + encryptionType.getValue());

            int resultColor = getResources().getColor(R.color.encryption_none);

            switch (encryptionType) {
                case ALL:
                    resultColor = getResources().getColor(R.color.encryption_all);
                    break;
                case HALF:
                    resultColor = getResources().getColor(R.color.encryption_half);
                    break;
                case NONE:
                    resultColor = getResources().getColor(R.color.encryption_none);
                    break;
            }

            return resultColor;
        }

        /**
         * 设置该项选中
         *
         * @param folder
         * @param isChecked
         */
        public void setChecked(FolderInfo folder, boolean isChecked) {
            if (isChecked) {
                if (!checkedList.contains(folder.getName())) {
                    checkedList.add(folder.getName());
                }
            } else {
                if (checkedList.contains(folder.getName())) {
                    checkedList.remove(folder.getName());
                }
            }
        }


    }

    class ViewHolder {
        public TextView txt_index;

        public TextView txt_name;

        public CheckBox cb_folder;

        public RelativeLayout layout_folder;
    }


}
