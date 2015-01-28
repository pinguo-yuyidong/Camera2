#pragma version(1)
#pragma rs java_package_name(com.example.android.rs.hellocompute)

rs_allocation pre;
rs_allocation input;
const static float3 gMonoMult={0.299f,0.587f,0.114f};
void root(const uchar4 *v_in, uchar4 *v_out) {
    //将一个uchar4 的颜色解压为float4
        float4 f4=rsUnpackColor8888(*v_in);
        //dot:[0]*[0]+[1]*[1]+[2]*[2]
        float3 mono=dot(f4.rgb,gMonoMult);
        //打包uchar4，alpha 默认为1.0
        *v_out=rsPackColorTo8888(mono);
}