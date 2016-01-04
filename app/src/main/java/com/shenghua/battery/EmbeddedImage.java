package com.shenghua.battery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by shenghua on 12/30/15.
 */
public class EmbeddedImage {

    private static final String batteryBase =
                "iVBORw0KGgoAAAANSUhEUgAAAgAAAABACAYAAABsv8+/AAAOPElEQVR42u2dS48lyVmG37jkpeuc\n" +
                "6q6qqWkkw+DGHmnwygOCBRLCLRlYsGHwLzCsWCCZf8ASeWW2bPCwsr2w+AUIZgsImw0gI+OxJfeA\n" +
                "h57uuvWpzIyIz4uMyIzMk6dc3e6hu6bfR4qKe0SeyC8jvsyIigAIIYQQ8sqhXmC9twBUmamv4X7W\n" +
                "uOJnXE8HoM1M85TuZ8mT3BsAQlEkhBDy0igAInIPwDsA/hDAvWh+bkIIaNoWTdOgaVq07dx0g7uZ\n" +
                "+btu2911Hbq2RddN07Ztg7Zt4bpu8Tre+dKX8PbnP4/PfObe1hAsswARgVIKIstjtVJqoXFVFj9N\n" +
                "N7evw666nyVOrvF7F/MvhM/tlGxXvEAmYb3BzB9NCBAIQhCEECDSu0VC5g8x3g/pkvHe93YICN7D\n" +
                "+wAffB/uA5zv3ck457bsZHzm7k2HrnO9/HVRDpN8Ng2appe/8/MzPHr0GI8ff4TN5hJNc4nLy0u0\n" +
                "bQut9aTdtTaDXGitYpie2QZaq5hGQyk9uHszho9hGsaYzG9gjJ74rTWxbA1j+vTG2FjWWB9UL9kp\n" +
                "fIhP/qWwWI8CEGS8R5Lfw2hDZHJvRXrpTGmTGe+xg/fjPRcJgz+Xk1xWercf+qO5neQzheWyOfWH\n" +
                "nc/cVc/ki2Iub9ceKJSCUnorTGuNuq5xdPQaDg4OUNU16miqqkJZVtEue1NVKIpi8Cd3bxcobIGi\n" +
                "SMbC2t4UhYUxyW+ibPYya7SBNqM8G22mz0GSRSjMu9ur+mOlFEIIuGwaXFw8kbOzM5yenOLRyYl8\n" +
                "9NEjefjwIR4+fCgf/uR/5YMPPpDTkxNxzsVxrRnGJue6oc8IIYj3fpBLAPA+bN2jdO1aGxRFoYqi\n" +
                "GNp0tVr3bVaWqipLVVUVbu3tqbq+hb29PbVarR6s1uvz/f39/7lz587fr9er79RV/d7vfvH+xfy+\n" +
                "2h3Cex/AXwC4Tx2JEEIIuRkI8KkQgvLev9W27Recq35kV+bf//lf/vXbv/kbv/43VyoAIvI1AH/O\n" +
                "ZiSEEMIRFRARJYL4JUrU5AtltF/g5cWvTcNnVEBE4hcoCUE+7Zz/RQBv/cd/fu8P6rr6k1+59+lT\n" +
                "ANCzwf/rHPwJIYSQG40CoERER7cNIXwWwG9Za7/1fw8f3pl8AYhv/l+el+Kcw8nJKTabDU7PzuG9\n" +
                "g3PT+dJhrjSbVw3ew+dzsNFO7nF+xMF1HbqFeVbvHbrOZXXG+mJdgx3dqY6u6+I8zCWapkHXucX5\n" +
                "uC/+3u+j7dp+fh9qmAefzN8tzGPn6wGm0+Oyc+48n2PPyxrLWZ4/n6eflDufj8R2GdcpZ9o0S/Oc\n" +
                "03Kx47ek9kjaaO7fGZbmd+dz/yLwcY54lB+BDx4S7RDneZfkLLevigtJfoY1Aj6bV45rBrybrBOY\n" +
                "rxdIRkLow0KAd65fw9I0uLy8RAgeZVkhhL4dQpBrr/+Yz1EDbjHddeZ301z+7vxqK31ai7Crnjzf\n" +
                "vPx+LcB2vrTWIc3Db73VPMMceprbf5Hkv/d53oflNh8xxizGbbvV4vqS/HrSvcnXnig15p+GK5Rl\n" +
                "ibquUca5/irO8+frR9I99d5DK4Uu9nv5s9R13TDnP8z9WwtjDazp5/6ttdP5f2Piepa0BiCtZdEw\n" +
                "WkObcU2LVvM1MtN1K0vrAAQYxqvNZoPz8wucnZ3h9PQUJycnODk5wePHj/Hhhx/iwYMf77zf6Zqf\n" +
                "agTP1l0YoyEiaNsW3p/Gdi9Q17dQ1zU2T5709mqDtt2HUgp7q1Xeq6t4Dz6llKqN1n8K4Ks2Bt6f\n" +
                "v/mLCM7OzvH45CQOyJ46FSGEEPKSkl6Az8/PgKi07e/vT9RJETnUxvxZ23Z/ndTDr8wH/5PTU1w8\n" +
                "ecIWJYQQQm4YEgQXFxc4PT3NQ/uPC8BdpfA7SQF4J8/YNA3f+AkhhJAbzmazwebych5slVJv6/j5\n" +
                "f/L237YdW40QQgj5BHBxsfU1Xw9/csIL/HcGQgghhDxfvF/+oq/ZNIQQQsirBxUAQgghhAoAIYQQ\n" +
                "Ql4F7HUTpk0b2rbD5eUluq7f6KTND+SJG/sMG/zMDlLJN32Y5pnm88NGQNPNVrquRdO08b8Uukmd\n" +
                "6XCW7c1b+gNBljbFadsWwft+q0c1PaAGWxvcjJvmTDe1kcUNhKab4mzHIfdj+TCc7XwA8HRxebnA\n" +
                "7mu7Mm64Tuy8NlwRJ/EC520zuDHdeEkECOkQl9BvCDQe9BM3BMo2lpof/LPoT5v9RPdwQFDu9x4h\n" +
                "+HiQTJKjeZiflCuZPYR7D+e64VCQ/KCgdIBQkt+n1tgXNolJ5IeKpMOClti1CU3amGpen/dqUm6+\n" +
                "TkhrPfi11oPspDpCCMNGMtPyZXLgjsjSoTzjvGUKy9P1/rC1+c+udUxPs74pHTpFdmNM//5YFAUO\n" +
                "D49weHiIw8NDrNf7k8N/8oN/iiK5LWxRorAWNh7+M9kAqBgPBeo3/ek3BRrTmMGdNtlJmwN5P/pN\n" +
                "CDBaw3s9bhYkGkZ6We5XwmkIFICFzbnSIVJ+PHiqc3HMa/tNvp5cXODBgx/jBz/474/vTV1vH8RU\n" +
                "FCVu3aqxXu/j8PAQx8ev47YIrLXY29t7PgpA2jEpPWhcJEgIIYS8PIQg6LoOl5tNvwOi0iiKEquf\n" +
                "Nb7vjFAKZVE88/GRhBBCCHkBCoEEbDZPcHFxceX22Iuju1IKVVVCcfAnhBBCbiTeOVxubwI0sDgF\n" +
                "UBQFRPipnxBCCLnJpHV01/4CYK1hqxFCCCGfECXg2goAIYQQQj4Z7Fq8TwWAEEIIeQWhAkAIIYRQ\n" +
                "ASCEEEIIFQBCCCGEUAEghBBCCBUAQgghhFABIIQQQggVAEIIIYRQASCEEEIIFQBCCCGEUAEghBBC\n" +
                "CBUAQgghhFABIIQQQggVAEIIIYRQASCEEEIIFQBCCCGEUAEghBBCCBUAQgghhFABIIQQQqgAEEII\n" +
                "IYQKACGEEEKoABBCCCGECgAhhBBCqAAQQgghhAoAIYQQQqgAEEIIIYQKACGEEEKoABBCCCGECgAh\n" +
                "hBBCqAAQQgghhAoAIYQQQqgAEEIIIYQKACGEEEKuqwC8nwcYY6CUYssQQgghnwDqqpoHCQBopdT7\n" +
                "cyVAa34YIIQQQm78W77WODw8nAeH9AUAAP42j1FKoSgKthwhhBByUwd/o3F8fIyqKudRXQjyXlIA\n" +
                "/grA4zzWWov99RplWbIVCSGEkBvEarXGG2/8Mm7fvp2/3gNAK4L/KsviPRvf+B+LyB8B+Ie8gKKw\n" +
                "OH7tCLf31zi/uEDXdXj06ATOdXDOo+s6OOei8XDewTsH5z38zATv4X1ACL2/bVu0bYuu6ybGOQfX\n" +
                "dehcX1YXy/fOoe06tE2DJsvnhnwdOtfBO99fi+v663AOIQSICOK0BwBARGCthdYaSvUxSimIyLAG\n" +
                "Inf3iQRQgJIxXV8uILHswT/YQ4V9GolXIdm1zMIly5/yjcmjO6Yb7VTSmFYklStZfF5HVl4fApGF\n" +
                "cJn+vkl8qn+oL6tjV9xQT1ZWXp4AQQIkBIQgCBIQQm4EIfjMjuE+wE/SJZkLQzrvA4KP7uD7PMnv\n" +
                "PXzKM5fhKLfeZWHOwXs3yJlzDt57OOcACJz3EOmvseu6TC5kvL8zltbf5GFKaSilZmFqZvfucSpP\n" +
                "DWEpXR/Xh/e2gtYqK7uP0zpfEzSmSfXkdWitAaWgYh2pHG1iuVBQun/YlFIIEgCR+HxieE6TXITg\n" +
                "M5kb5TOEkIVP84xhc9me5kMm/2OfMD5jIWzfo133bImnSfv/yfNc32VMf+/LskRVVaiqCtYWMNbC\n" +
                "bpkCRbFtbO62NrMtiqKMec1QTmFtLN/Amt42ZofRGsYaaKWhtIYxGlpraKWhtYp9f5L53W2jlII2\n" +
                "Opab/5YSZVmgrCqsVmscHBzG5z1ApO+nkiylZ36U5atlJX9WlVIwxgzXm9xFUaKua6xWa6zXa+zv\n" +
                "7+Pw6Ah37hzg+PVjHBwc5l/yJY7+QSn1kfPuLwHAZhX+o4j8MYCvzy+mLEscxS8Bv3D37nMRnqGT\n" +
                "Fhk6+tRooy1jGgmQOBj0dp9PJA/ry5RZWbsexuPj17HeX8Nae/XDfN2H6xodws/zAF7VqTxt3DxI\n" +
                "sFsoJ/kXwud2SrYrXrIBMO+8p/5oQoDEhygNEKOMhFFGJkrBqAQMg3sIgxKaBvPgw5aymg/iyXaZ\n" +
                "EuompkPXuaiMjkpp27ZomwZ13aBtW5yfWzjnIXI++x1ha72NUjobpHtZSWlG2wwDcd8p6MHdmzF8\n" +
                "DNNDx9EbM3aI0W+tiWX3nWXq8PKOMh/kU/gQn/xLYbEeBSDIeI8kv4fRHpWC0RZgSJvMeI9dpuj1\n" +
                "nW/y53KSy0pSzHLFILeTfKawqSKCiQKy65l7GZWAZ13f1d9/vT0wao26rnF09BoODg5Q1TXqaKqq\n" +
                "QllW0S57U1UoimLwJ3dvFygmisKoQBSFjYPvdNC31sBoA21GeTbaTJ+DJIuDwosrlGe1FW+MQVn2\n" +
                "g+16vZq9+PbmzTffxN27d9F1LZqmRdM0aNsG3of4wtynS31SkksA8D5s3aN07VqboT3quoa1Bdbr\n" +
                "FaqqHtqwqmvcunUrmj2sVisUtpgNXwoAvNbqhxLC3712dPSNiQIQf+y7IvLdqAS8/XEL4sex2PBp\n" +
                "HrrrDsb8nwhCCCFxQFh6Zc8H2uHL2vP/gjMpVy3HDw6JY5xSCo+MMd8XkW+/8cYvfTWltwuD4ncB\n" +
                "/JqIfBnAVz5uRYAQQgh5iQd8KKVEKaTpLYkGSmvJ/DJ86Ypf37z3W++oz3YF0xdnFecslIoTbWq8\n" +
                "OgyDvoJS6kfW2veNsf8UQvjm5371re/kBdsr3o7fBfCuiNwDcB/APQB3qBAQQgh5RZErbHmK/M+k\n" +
                "AOwIV9m4/T2llDXGPCzL8idlWX5fKfVv97/w2z9cKuCnvUeGcRp462UAAAAASUVORK5CYILJ7z18\n" +
                "yjOX4Si33mVhzsF7N8iZcw7eezjnAAic9xDpr7HrukwuZLy/M5bW3+RhSmkopWZhamb37nEqTw1h\n" +
                "KV0f14f3toLWKiu7j9M6XxM0pkn15HVorQGloGIdqRxtYrlQULp/2JRSCBIAkfh8YnhOk1yE4DOZ\n" +
                "G+UzhJCFT/OMYXPZnuZDJv9jnzA+YyFs36Nd92yJp0n7/8nzXN9lTH/vy7JEVVWoqgrWFjDWwm6Z\n" +
                "AkWxbWzutjazLYqijHnNUE5hbSzfwJreNmaH0RrGGmilobSGMRpaa2ilobWKfX+S+d1to5SCNjqW\n" +
                "m/+WEmVZoKwqrFZrHBwcxuc9QKTvp5IspWd+lOWrZSV/VpVSMMYM15vcRVGirmusVmus12vs7+/j\n" +
                "8OgId+4c4Pj1YxwcHOZf8iWO/kEp9ZHz7i8BwGYV/qOI/DGAr88vpixLHMUvAb9w9+5zEZ6hkxYZ\n" +
                "OvrUaKMtYxoJkDgY9HafTyQP68uUWVm7Hsbj49ex3l/DWnv1w3zdh+saHcLP8wBe1Q==\n";

    private static final String batteryLiquidTop =
            "iVBORw0KGgoAAAANSUhEUgAAAeAAAAAMCAMAAACN+57qAAAC31BMVEUAAAD/////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "////////////////////////////////////////////////////////////////////////////\n" +
            "//////////////////////////////////////////////92mWl0AAAA9XRSTlMACxYXGBktNDU2\n" +
            "Nzg5Ojs8RFtcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnOam5ydn6ChoqOkpaanqaqrrK6vsLGztLW2\n" +
            "t7i6u7y+v8HCw8TGx8jJy8zNz9DS09TW19ja293e3+Hi5OXn6Onr7O3v8fLw7ubj3NXRzsrFwLmy\n" +
            "raiemZh6WllCMzIsECEiIyhFT1BRUlNUVVZXWHR1d3h5e3x9fn+AgYOEhYaHiImKi4yNjo+RkpOU\n" +
            "lZaXvdng8/X06pCCdk5NQysgDwQDDQ4cKj0+P0BBR0hJSktM9vf4+fr7/EYpHxsHChonLhMmBjEk\n" +
            "ERQVHjAdBf4fLLwAAAYxSURBVFjD7diJO9R5HAdwTcqYMYyb6dKhULpPHTpJ6SJJyJWYVJKjolUY\n" +
            "pDDuI0cJldKdTnQM2R2ZweTKsSO71cpubbt/wH4+398M2vU8PbtP+3Q8Pv/B7/f6vr/f7/uroPC/\n" +
            "zxAabSiOIplhMMOVlJTodLqyMoPBYKqoqLBYqqpqamx1DU0tbW1tHV09fX3OiJGjRo8eYzB27Ljx\n" +
            "EwwnTpxkZGwyeYrp1GnTZ8ycNXvO3HnzF5gtXLR4ifnSZctXrFxlYbnaas1a63XrN2zcZGO72W6L\n" +
            "/VaHbY5OzttdXN3cPXZ47vTy5u7y2b1n7x7ffT5cby8/z/0e7m6u/tudnbYFbLUPtAvabHNg04aD\n" +
            "66zXHrJabWmxKnjF8qXmIUsWLTxstmDed3PnzJo5Y/rUUNMpk02OHA2baDhhwvhxYw0MxoweNXLE\n" +
            "CI6+np6ujraWpqaGhnp4BE+VxVJRYTKZDIayMp0O3xoZGRUVPYx8PvkTQ2k4xxS+0Dkmn2ExMcdh\n" +
            "TsDEUlhMJmrFxfP5CYmJSUnJKSmpaenpEeHh6hoETxfsOGg3Cu2ADuwmGRkZZ2SePJmVHZaTe+p0\n" +
            "Xt6Z/ILCs+fOF124WHzp8pWr167fuFly6/bRI/2BzczRNXiVxZ3VVofWrF13EGQPgGzQlsDArQ4B\n" +
            "25zuOm/3d3W7575/h6ffTm+i67tn7/3SsvtI7MMt99rp57nDw/2eqwso33UkzFuCNtui83qAXgPQ\n" +
            "dwj0sqUhSxYvWji7F/h2yc0b1689uHrl8qXiixfOnztbWPAw/0ze6VO5OWHZWScfZWZkGBtNglUg\n" +
            "XwSwCjgcPV1cBFpkEYSnp6elpqakJCclJSYk8OPjHwsEFX1rgk6vxB97PAbmiSL511Uw3386wx+E\n" +
            "wurqasWnZGoqYaKjIzFlaiKRSCwW19bW1dXX83gVFWyJ5FlDY2NTU7NA8PhxPD8hISkNRNUxjVo6\n" +
            "BJRk0cDQyDgz81FWVnZOTu5pQHyIikWgiIgPjk4xDZ0KdpDN7yCchzGcIRhOMLQkiNYUIlG0oxQd\n" +
            "CSM4AqQHkYSglnMBc99u4ATPsrLS0pbnz1tb29ra2zs6On7E6Whvb29ra2193tJSCuB7IdW+u/f5\n" +
            "7NrFhWh7+YE7pJvI+zvfRXoHtLcLsrWx2bSxD9/SInilTF8e89kQ82mhpqa4Cm5dL75QRPDz88+A\n" +
            "fa7MXkZvwNHXl8cewdPSUlOSk5PiBYLmpqbGxoZnzyRSqTSOza6vq6utrRWLRSL4+6qqsDuwlCsr\n" +
            "a2A6Xzx92tXVpVgthPnpI6I/C4UvX76MefXqVY0S7p0kkLLtkwWwRJWgVsRJpTJTgSCeZBWTCqrA\n" +
            "qklUZahURg0hoRmZkNDssFy5LMazuPgSpBPiiek0Mfn2gEtuUimHkAN0YSEJuTzjxsbyhOMuz+Ho\n" +
            "Y7xxiw+PiEhPw2gnJvD5fPAm3A0SSRxgsyt4qC0i2GpwDlChZ6AXHgavwS8GHAG8n+0vT7ojyZBz\n" +
            "EY9GBoPabpmAy1KT6dbWom4FO24Q+HMC8zDRYgy0SBXzLDeW7+145lOc3SdoFDCte7gMmD4I/A0B\n" +
            "D++m9aX4jVBIIzt07Ove2+0g8NcCzOgF7omNjf2V7NFvPnYiC6t/6+oiV6wacsli4kEsoqB5PLbc\n" +
            "GaCb8WaVKLsxkwuz1iDwfwMGW3K1JhdrAdy3mhsk0jikreDVg2wddfrCRSsaSDo7O1904bzFW5bw\n" +
            "U5XaqiqqI719gpd4UpMq+6Ve3CzP9T9qkg6iy9QN+5ejnFMIX/D3ThSKnWiWjNwMzUOo1htMau8h\n" +
            "7L12gfb2DgEBSO3s7+LqCtIUNUiXE2lfbEf3y0pbWtAZZRG2DGBlrlxuuTfpTPs9PO65ubm4AKuT\n" +
            "o2Mvq6211R0sxisB05wUpsML5hNO6MbTSHWC5nSrpF9zKjqfJy9MKEkoxxmMIa2Z3J4HqknNcH2u\n" +
            "l++0sM++ltUkGEWqJlVVDfniOvM7Uun7Hjp+7+mJioqK/PChQ5JKbfADPXScHbgHf9aHjr4eXDDQ\n" +
            "Q0cE1XmkHz50RPX09Lx//0e/hw7aO5g/FQbn30535AcLiCkve9jjqTLJ6n1qYpB+QV1MooSD/25w\n" +
            "vrr5C38l2iwBgGY3AAAAAElFTkSuQmCC4osXzp87W1jwMP9M3ulTuTlh2VknH2VmZBgbTYJVIF8E\n" +
            "sAo4HD1dXARaZBGEp6enpaampCQnJSUmJPDj4x8LBBV9a4JOr8QfezwG5oki+ddVMN9/OsMfhMLq\n" +
            "6mrFp2RqKmGioyMxZWoikUgsFtfW1tXV1/N4FRVsieRZQ2NjU1OzQPD4cTw/ISEpDUTVMY1aOgSU\n" +
            "ZNHA0Mg4M/NRVlZ2Tk7uaUB8iIpFoIiID45OMQ2dCnaQze8gnIcxnCEYTjC0JIjWFCJRtKMUHQkj\n" +
            "OAKkB5GEoJZzAXPfbuAEz7Ky0tKW589bW9va2ts7Ojp+xOlob29va2ttfd7SUgrgeyHVvrv3+eza\n" +
            "xYVoe/mBO6SbyPs730V6B7S3C7K1sdm0sQ/f0iJ4pUxfHvPZEPNpoaamuApuXS++UETw8/PPgH2u\n" +
            "zF5Gb8DR15fHHsHT0lJTkpOT4gWC5qamxsaGZ88kUqk0js2ur6urra0Vi0Ui+PuqqrA7sJQrK2tg\n" +
            "Ol88fdrV1aVYLYT56SOiPwuFL1++jHn16lWNEu6dJJCy7ZMFsESVoFbESaUyU4EgnmQV\n";

    private static final String batteryLiquidCover =
            "iVBORw0KGgoAAAANSUhEUgAAAfgAAAABCAQAAACYy3SRAAAAxklEQVQoz5WSTQ6CMBCFv5aCwXAI\n" +
            "l268jHfwnN7EtQtiYiSGpFBcEEp/CfZl3nuTDKSdGWFuXHnwRKMZ0AwWIyOjVWNjxmR5CoAXC4fn\n" +
            "wJkLJ6RTE7Lv5shlKc7rlk/leeyrNH/kKT/rOoGl+/4tvG7LdN+DYwQCicDV0LvA0SkzhXSWm2m8\n" +
            "KzHHu5RyiV3b1wUw68skBQpFSYl0UOx08Rdb9aF2tLS8ePOh40tPj2ZgxOTeYgTY2wv7P4VCUVFR\n" +
            "U3OkoeH+AyYl5xnB2CIoAAAAAElFTkSuQmCCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==\n";

    public static Bitmap createBatteryBaseImage() {
        return decodeBase64(batteryBase);
    }

    public static Bitmap createBatteryLiquidTopImage() {
        return decodeBase64(batteryLiquidTop);
    }

    public static Bitmap createBatteryLiquidCoverImage() {
        return decodeBase64(batteryLiquidCover);
    }

    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;

//        Bitmap after = BitmapFactory.decodeByteArray(b, 0, b.length);
//        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
//        after.compress(Bitmap.CompressFormat.PNG, 100, baos1);
//        byte[] b1 = baos.toByteArray();
//        Log.d("===>encode test", "b:"+b.length+", b1:"+b1.length);
//        Log.d("===> encode test", b.equals(b1) ? "pass" : "failed");
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static void encodeResourceToFile(Context context,
                                            String resouceName,
                                            String saveFileName,
                                            String compareString) {
        try {
            InputStream is = context.getResources().openRawResource(
                    context.getResources().getIdentifier(resouceName, "drawable", context.getPackageName())
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            while(is.available() > 0) {
                is.read(bytes);
                baos.write(bytes);
            }
            is.close();

            byte[] imageBytes = baos.toByteArray();

            String imageEncoded = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.e("===>resouce encode", "encoded string lenght: " + imageEncoded.length());

            // this print code can only be used to view
            // it is not usable as Log.d() may print \n sign will corrupt orginal string
            /*int len = imageEncoded.length();
            int logMaxLen = 64;
            for (int i = 0; i < len; i += logMaxLen) {
                int end = i + logMaxLen;
                end = end < len ? end : len;
                Log.d("===>", imageEncoded.substring(i, end));
            }*/

            // save string to file
            FileOutputStream fos = context.openFileOutput(saveFileName, Context.MODE_PRIVATE);
            fos.write(imageEncoded.getBytes());
            fos.close();
            Log.e("===>resource encode", "result saved to " + saveFileName);

            Log.e("===>resource encode", "encoded:" + imageEncoded.length() + ", compared string:" + compareString.length());
            Log.e("===>resource encode", "compare check: "+ (imageEncoded.equals(compareString) ? "pass" : "failed"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encodeDrawableResouce(Context context) {
        //encodeResourceToFile(context, "battery_base", "battery_base.txt", batteryBase);
        //encodeResourceToFile(context, "battery_liquid_top", "battery_liquid_top.txt", batteryLiquidTop);
        //encodeResourceToFile(context, "battery_liquid_cover", "battery_liquid_cover.txt", batteryLiquidCover);
    }
}
