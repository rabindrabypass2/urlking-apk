package com.rabindra.urlking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnAirplane;
    private Button btnReload;
    private TextView tvStatus;
    private TextView tvIp;
    private ProgressBar progressBar;
    private PowerManager.WakeLock wakeLock;

    private static final String TARGET_URL = "https://urlking.in/b0f761";
    private static final Random RANDOM = new Random();

    // Paired Per-Cycle Device Profiles (UA + CPU + RAM + GPU + Screen) so every IP rotation gets a unique, internally consistent browser fingerprint
    private static class DeviceProfile {
        final String ua;
        final int cores;
        final int memGb;
        final String gpuVendor;
        final String gpuRenderer;
        final int screenW;
        final int screenH;

        DeviceProfile(String ua, int cores, int memGb, String gpuVendor, String gpuRenderer, int screenW, int screenH) {
            this.ua = ua;
            this.cores = cores;
            this.memGb = memGb;
            this.gpuVendor = gpuVendor;
            this.gpuRenderer = gpuRenderer;
            this.screenW = screenW;
            this.screenH = screenH;
        }
    }

    private static final DeviceProfile[] DEVICE_PROFILES = new DeviceProfile[]{
        // Flagship & Upper Mid-Range
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 9, 12, "ARM", "Mali-G715", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 12, "Qualcomm", "Adreno (TM) 750", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; CPH2581) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Mobile Safari/537.36", 8, 16, "Qualcomm", "Adreno (TM) 750", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G710 MC10", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 12, "ARM", "Mali-G78 MP14", 384, 854),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; NE2215) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 730", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; 2211133G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 740", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; CPH2449) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 16, "Qualcomm", "Adreno (TM) 740", 412, 915),
        // Samsung A / M / F Series
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-A556B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "Samsung", "Samsung Xclipse 530", 384, 832),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-A546E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G68 MP5", 384, 832),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-M346B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.86 Mobile Safari/537.36", 8, 6, "ARM", "Mali-G68 MP4", 384, 832),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; SM-M146B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36", 8, 6, "ARM", "Mali-G68 MP2", 360, 800),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-E546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G68 MP5", 384, 832),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; SM-A156E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Mobile Safari/537.36", 8, 6, "ARM", "Mali-G57 MC2", 360, 800),
        // Redmi / Xiaomi / POCO
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; 23090RA98G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G615 MC6", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; 2311DRK48I) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 12, "ARM", "Mali-G615 MC6", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; 23049PCD8I) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 725", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; 2201116PI) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36", 8, 6, "Qualcomm", "Adreno (TM) 619", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; 23076RN4BI) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.86 Mobile Safari/537.36", 8, 6, "Qualcomm", "Adreno (TM) 613", 360, 800),
        // Vivo / iQOO
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; V2319) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 740", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; I2219) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 644", 393, 873),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; V2250) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G610 MC6", 393, 873),
        // Realme / OnePlus Nord / OPPO
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; RMX3840) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.86 Mobile Safari/537.36", 8, 12, "Qualcomm", "Adreno (TM) 735", 390, 844),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; RMX3771) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G68 MC4", 390, 844),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; CPH2613) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 732", 390, 844),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; CPH2493) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36", 8, 8, "Qualcomm", "Adreno (TM) 642L", 390, 844),
        // Nothing / Motorola / Infinix / Tecno
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; A065) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 12, "Qualcomm", "Adreno (TM) 730", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; motorola edge 50 pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36", 8, 12, "Qualcomm", "Adreno (TM) 720", 412, 915),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 14; Infinix X6850) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.86 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G57 MC2", 360, 800),
        new DeviceProfile("Mozilla/5.0 (Linux; Android 13; TECNO CK7n) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36", 8, 8, "ARM", "Mali-G57 MC2", 360, 800)
    };

    private int lastProfileIdx = -1;
    private volatile DeviceProfile activeProfile = DEVICE_PROFILES[0];
    private volatile String currentSpoofJs = "";

    private void rotateDeviceProfile() {
        int nextIdx = RANDOM.nextInt(DEVICE_PROFILES.length);
        if (DEVICE_PROFILES.length > 1 && nextIdx == lastProfileIdx) {
            nextIdx = (nextIdx + 1 + RANDOM.nextInt(DEVICE_PROFILES.length - 1)) % DEVICE_PROFILES.length;
        }
        lastProfileIdx = nextIdx;
        DeviceProfile p = DEVICE_PROFILES[nextIdx];
        activeProfile = p;
        int canvasXor = 1 + RANDOM.nextInt(250);
        int canvasCh = RANDOM.nextInt(3);
        double audioDelta = (1 + RANDOM.nextInt(95)) * 1e-7;
        int touchPts = RANDOM.nextBoolean() ? 5 : 10;
        String safeVendor = p.gpuVendor.replace("'", "");
        String safeRenderer = p.gpuRenderer.replace("'", "");

        String androidVer = "14";
        String deviceModel = "Pixel 8 Pro";
        String chromeFull = "131.0.6778.135";
        String chromeMajor = "131";
        Matcher mVer = Pattern.compile("Android (\\d+); ([^)]+)\\)").matcher(p.ua);
        if (mVer.find()) {
            androidVer = mVer.group(1).replace("'", "");
            deviceModel = mVer.group(2).replace("'", "");
        }
        Matcher mChr = Pattern.compile("Chrome/(\\d+)\\.([\\d.]+)").matcher(p.ua);
        if (mChr.find()) {
            chromeMajor = mChr.group(1);
            chromeFull = chromeMajor + "." + mChr.group(2);
        }
        String dpr = (p.screenW >= 412) ? "2.625" : (p.screenW >= 390 ? "3.0" : "2.0");

        currentSpoofJs =
            "(function(){" +
            "try{" +
            "var _sf=Symbol.for('_uk_fp');" +
            "if(window[_sf])return;" +
            "window[_sf]=1;" +
            "var _nm=new WeakMap();" +
            "var _ots=Function.prototype.toString;" +
            "var _nts=function toString(){if(typeof this==='function'&&_nm.has(this)){return 'function '+_nm.get(this)+'() { [native code] }';}return _ots.call(this);};" +
            "_nm.set(_nts,'toString');" +
            "try{Object.defineProperty(Function.prototype,'toString',{value:_nts,writable:true,configurable:true});}catch(e){}" +
            "function _mk(fn,n){try{_nm.set(fn,n);}catch(e){}return fn;}" +
            "function _def(obj,prop,fn,gName){try{_mk(fn,'get '+gName);Object.defineProperty(obj,prop,{get:fn,enumerable:true,configurable:true});}catch(e){}}" +
            "var _np=typeof Navigator!=='undefined'?Navigator.prototype:navigator;" +
            "_def(_np,'webdriver',function(){return false;},'webdriver');" +
            "_def(_np,'hardwareConcurrency',function(){return " + p.cores + ";},'hardwareConcurrency');" +
            "_def(_np,'deviceMemory',function(){return " + p.memGb + ";},'deviceMemory');" +
            "_def(_np,'maxTouchPoints',function(){return " + touchPts + ";},'maxTouchPoints');" +
            "_def(_np,'language',function(){return 'en-IN';},'language');" +
            "_def(_np,'languages',function(){return['en-IN','en-US','en'];},'languages');" +
            "var _conn={effectiveType:'4g',rtt:50,downlink:10,saveData:false,onchange:null};" +
            "_def(_np,'connection',function(){return _conn;},'connection');" +
            "var _uadBrands=[{brand:'Chromium',version:'" + chromeMajor + "'},{brand:'Google Chrome',version:'" + chromeMajor + "'},{brand:'Not-A.Brand',version:'24'}];" +
            "var _uadFull=[{brand:'Chromium',version:'" + chromeFull + "'},{brand:'Google Chrome',version:'" + chromeFull + "'},{brand:'Not-A.Brand',version:'24.0.0.0'}];" +
            "var _uad={brands:_uadBrands,mobile:true,platform:'Android'," +
            "getHighEntropyValues:_mk(function(hints){return Promise.resolve({architecture:'',bitness:'',brands:_uadBrands,fullVersionList:_uadFull,mobile:true,model:'" + deviceModel + "',platform:'Android',platformVersion:'" + androidVer + ".0.0',uaFullVersion:'" + chromeFull + "',wow64:false});},'getHighEntropyValues')," +
            "toJSON:_mk(function(){return{brands:_uadBrands,mobile:true,platform:'Android'};},'toJSON')};" +
            "_def(_np,'userAgentData',function(){return _uad;},'userAgentData');" +
            "var _sp=typeof Screen!=='undefined'?Screen.prototype:screen;" +
            "_def(_sp,'width',function(){return " + p.screenW + ";},'width');" +
            "_def(_sp,'height',function(){return " + p.screenH + ";},'height');" +
            "_def(_sp,'availWidth',function(){return " + p.screenW + ";},'availWidth');" +
            "_def(_sp,'availHeight',function(){return " + (p.screenH - 48) + ";},'availHeight');" +
            "_def(_sp,'colorDepth',function(){return 24;},'colorDepth');" +
            "_def(_sp,'pixelDepth',function(){return 24;},'pixelDepth');" +
            "_def(window,'devicePixelRatio',function(){return " + dpr + ";},'devicePixelRatio');" +
            "if(!window.chrome){" +
            "window.chrome={" +
            "runtime:{id:undefined,connect:_mk(function(){},'connect'),sendMessage:_mk(function(){},'sendMessage'),onMessage:{addListener:_mk(function(){},'addListener')}}," +
            "loadTimes:_mk(function(){return{requestTime:0,startLoadTime:0,commitLoadTime:0,finishDocumentLoadTime:0,finishLoadTime:0,firstPaintTime:0,navigationType:'Other',wasFetchedViaSpdy:false,wasNpnNegotiated:false,npnNegotiatedProtocol:'',wasAlternateProtocolAvailable:false,connectionInfo:'http/1.1'};},'loadTimes')," +
            "csi:_mk(function(){return{startE:Date.now(),onloadT:Date.now(),pageT:0,tran:15};},'csi')," +
            "app:{isInstalled:false,getDetails:_mk(function(){},'getDetails'),getIsInstalled:_mk(function(){},'getIsInstalled'),installState:_mk(function(){},'installState'),runningState:_mk(function(){return 'cannot_run';},'runningState')},tabs:{}" +
            "};}" +
            "_def(_np,'plugins',function(){var a=[{name:'Chrome PDF Plugin',filename:'internal-pdf-viewer',description:'Portable Document Format'},{name:'Chrome PDF Viewer',filename:'mhjfbmdgcfjbbpaeojofohoefgiehjai',description:''}];a.refresh=function(){};return a;},'plugins');" +
            "try{" +
            "var _gp1=WebGLRenderingContext.prototype.getParameter;" +
            "WebGLRenderingContext.prototype.getParameter=_mk(function(param){if(param===0x9245)return '" + safeVendor + "';if(param===0x9246)return '" + safeRenderer + "';return _gp1.apply(this,arguments);},'getParameter');" +
            "if(window.WebGL2RenderingContext){var _gp2=WebGL2RenderingContext.prototype.getParameter;WebGL2RenderingContext.prototype.getParameter=_mk(function(param){if(param===0x9245)return '" + safeVendor + "';if(param===0x9246)return '" + safeRenderer + "';return _gp2.apply(this,arguments);},'getParameter');}" +
            "}catch(e){}" +
            "try{" +
            "var _td=HTMLCanvasElement.prototype.toDataURL;" +
            "HTMLCanvasElement.prototype.toDataURL=_mk(function(){try{if(this.width>0&&this.height>0){var tmp=document.createElement('canvas');tmp.width=this.width;tmp.height=this.height;var tc=tmp.getContext('2d');if(tc){tc.drawImage(this,0,0);var d=tc.getImageData(0,0,Math.min(8,tmp.width),Math.min(8,tmp.height));if(d&&d.data.length>4){d.data[" + canvasCh + "]=(d.data[" + canvasCh + "]^" + canvasXor + ");tc.putImageData(d,0,0);return _td.apply(tmp,arguments);}}}}catch(e){}return _td.apply(this,arguments);},'toDataURL');" +
            "}catch(e){}" +
            "try{" +
            "var _dp=typeof Document!=='undefined'?Document.prototype:document;" +
            "_def(_dp,'hidden',function(){return false;},'hidden');" +
            "_def(_dp,'webkitHidden',function(){return false;},'webkitHidden');" +
            "_def(_dp,'visibilityState',function(){return 'visible';},'visibilityState');" +
            "document.hasFocus=_mk(function(){return true;},'hasFocus');" +
            "var _wal=window.addEventListener;window.addEventListener=_mk(function(t,l,o){if(t==='visibilitychange'||t==='webkitvisibilitychange'||t==='pagehide')return;return _wal.call(this,t,l,o);},'addEventListener');" +
            "var _dal=document.addEventListener;document.addEventListener=_mk(function(t,l,o){if(t==='visibilitychange'||t==='webkitvisibilitychange'||t==='pagehide')return;return _dal.call(this,t,l,o);},'addEventListener');" +
            "}catch(e){}" +
            "try{" +
            "if(window.AudioBuffer&&AudioBuffer.prototype.getChannelData){var _acd=AudioBuffer.prototype.getChannelData;AudioBuffer.prototype.getChannelData=_mk(function(){var r=_acd.apply(this,arguments);try{if(r&&r.length>0&&!r.__fp){Object.defineProperty(r,'__fp',{value:1,configurable:true});r[0]=r[0]+" + audioDelta + ";}}catch(e){}return r;},'getChannelData');}" +
            "}catch(e){}" +
            "}catch(ex){}" +
            "})();";
    }

    // Embedded UrlKing Automation Script:
    // 1. Spoofs organic Telegram referrer and blocks popup ad hijacking while registering silent ad pings
    // 2. Runs direct 5-step session dwell engine on urlking.in / go.urlking.in
    // 3. On get.urlking.in: waits for GET LINK button (#getLinkBtn) with AES-CBC API fallback
    // 4. Captures MediaFire destination -> triggers root 4G/5G IP + device rotation
    private static final String SCRIPT_URLKING =
        "(function(){\n" +
        "    var _si = Symbol.for('_uk_sc');\n" +
        "    if (window[_si]) return;\n" +
        "    window[_si] = 1;\n" +
        "\n" +
        "    var _emit = (function(){\n" +
        "        var _cd = console.debug.bind(console);\n" +
        "        return function(tag, val) {\n" +
        "            try { _cd('__UK_BR__:' + tag + (val !== undefined ? ':' + val : '')); } catch(e){}\n" +
        "        };\n" +
        "    })();\n" +
        "\n" +
        "    // 1. Spoof Organic Telegram Referrer in DOM\n" +
        "    try {\n" +
        "        Object.defineProperty(document, 'referrer', {\n" +
        "            get: function() { return 'https://t.me/'; },\n" +
        "            configurable: true\n" +
        "        });\n" +
        "    } catch(e){}\n" +
        "\n" +
        "    var isMediafireFound = false;\n" +
        "\n" +
        "    function isAdNetwork(u) {\n" +
        "        if (!u) return false;\n" +
        "        var l = (u + '').toLowerCase();\n" +
        "        return l.indexOf('omg10.com') !== -1 || l.indexOf('popads') !== -1 || l.indexOf('adsterra') !== -1 || " +
        "l.indexOf('monetag') !== -1 || l.indexOf('propellerads') !== -1 || l.indexOf('hilltopads') !== -1 || " +
        "l.indexOf('clickadu') !== -1 || l.indexOf('exoclick') !== -1 || l.indexOf('adtrue') !== -1 || " +
        "l.indexOf('ey43.com') !== -1 || l.indexOf('doubleclick') !== -1 || l.indexOf('syndication') !== -1;\n" +
        "    }\n" +
        "\n" +
        "    function registerAdImpression(u) {\n" +
        "        try {\n" +
        "            if (!u || !isAdNetwork(u)) return;\n" +
        "            var ifr = document.createElement('iframe');\n" +
        "            ifr.style.cssText = 'position:absolute;width:1px;height:1px;left:-9999px;top:-9999px;visibility:hidden;border:none;';\n" +
        "            ifr.src = u;\n" +
        "            (document.body || document.documentElement).appendChild(ifr);\n" +
        "            setTimeout(function() {\n" +
        "                try { ifr.remove(); } catch(e){}\n" +
        "            }, 7000);\n" +
        "        } catch(e){}\n" +
        "    }\n" +
        "\n" +
        "    function notifyDestination(url) {\n" +
        "        if (!url || isMediafireFound) return;\n" +
        "        if (url.indexOf('mediafire.com') !== -1) {\n" +
        "            isMediafireFound = true;\n" +
        "            _emit('DEST', url);\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    // Intercept window.open: capture MediaFire URL and silently ping ad\n" +
        "    var fakeTab = {\n" +
        "        location: {\n" +
        "            _href: 'about:blank',\n" +
        "            set href(val) {\n" +
        "                this._href = val;\n" +
        "                if (val && val !== 'about:blank') {\n" +
        "                    if (val.indexOf('mediafire.com') !== -1) {\n" +
        "                        notifyDestination(val);\n" +
        "                    } else if (isAdNetwork(val)) {\n" +
        "                        registerAdImpression(val);\n" +
        "                    }\n" +
        "                }\n" +
        "            },\n" +
        "            get href() { return this._href; }\n" +
        "        },\n" +
        "        closed: false,\n" +
        "        close: function() { this.closed = true; }\n" +
        "    };\n" +
        "\n" +
        "    try {\n" +
        "        var _origReplace = window.location.replace;\n" +
        "        window.location.replace = function(u) {\n" +
        "            if (isAdNetwork(u)) {\n" +
        "                registerAdImpression(u);\n" +
        "                return;\n" +
        "            }\n" +
        "            if (_origReplace) _origReplace.call(window.location, u);\n" +
        "            else window.location.href = u;\n" +
        "        };\n" +
        "    } catch(e){}\n" +
        "\n" +
        "    try {\n" +
        "        var _origOpen = window.open;\n" +
        "        window.open = function(url, target) {\n" +
        "            if (url && url !== 'about:blank' && !url.startsWith('javascript:')) {\n" +
        "                if (url.indexOf('mediafire.com') !== -1) {\n" +
        "                    notifyDestination(url);\n" +
        "                    return fakeTab;\n" +
        "                }\n" +
        "                if (isAdNetwork(url)) {\n" +
        "                    registerAdImpression(url);\n" +
        "                    return fakeTab;\n" +
        "                }\n" +
        "                notifyDestination(url);\n" +
        "            }\n" +
        "            return fakeTab;\n" +
        "        };\n" +
        "    } catch(e){}\n" +
        "\n" +
        "    // ========================================================\n" +
        "    // STAGE A: get.urlking.in (GET LINK PAGE)\n" +
        "    // ========================================================\n" +
        "    if (location.hostname === 'get.urlking.in') {\n" +
        "        _emit('STATUS', 'GET LINK Page: Verifying timer...');\n" +
        "\n" +
        "        function isGetLinkReady() {\n" +
        "            var btn = document.getElementById('getLinkBtn');\n" +
        "            var area = document.getElementById('final-btn-area');\n" +
        "            if (!btn || !area) return false;\n" +
        "            if (area.classList.contains('hidden')) return false;\n" +
        "            if (btn.disabled) return false;\n" +
        "            var txt = (btn.innerText || '').toUpperCase();\n" +
        "            if (txt.indexOf('GET') !== -1 || txt.indexOf('RETRY') !== -1) {\n" +
        "                if (txt.indexOf('GETTING') === -1 && txt.indexOf('VERIFYING') === -1) {\n" +
        "                    return true;\n" +
        "                }\n" +
        "            }\n" +
        "            return false;\n" +
        "        }\n" +
        "\n" +
        "        var clickPending = false;\n" +
        "        async function tryClickGetLink() {\n" +
        "            if (clickPending || isMediafireFound) return;\n" +
        "            if (isGetLinkReady()) {\n" +
        "                clickPending = true;\n" +
        "\n" +
        "                var delay = 1000 + Math.floor(Math.random() * 800);\n" +
        "                _emit('STATUS', 'Clicking GET LINK (' + (delay/1000).toFixed(1) + 's)...');\n" +
        "                await new Promise(r => setTimeout(r, delay));\n" +
        "\n" +
        "                _emit('GETLINK');\n" +
        "\n" +
        "                var btn = document.getElementById('getLinkBtn');\n" +
        "                if (btn && isGetLinkReady()) {\n" +
        "                    try { btn.focus(); } catch(e){}\n" +
        "                    try { btn.dispatchEvent(new MouseEvent('mouseover', {bubbles: true, cancelable: true, view: window})); } catch(e){}\n" +
        "                    try { btn.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true, view: window})); } catch(e){}\n" +
        "                    try { btn.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true, view: window})); } catch(e){}\n" +
        "                    try { btn.click(); } catch(e){}\n" +
        "                }\n" +
        "\n" +
        "                setTimeout(function() {\n" +
        "                    clickPending = false;\n" +
        "                }, 4000);\n" +
        "            }\n" +
        "        }\n" +
        "\n" +
        "        setInterval(tryClickGetLink, 300);\n" +
        "\n" +
        "        setTimeout(async function() {\n" +
        "            if (isMediafireFound || location.href.indexOf('mediafire.com') !== -1) return;\n" +
        "            var q = new URLSearchParams(location.search);\n" +
        "            var h = new URLSearchParams(location.hash.replace(/^#/, ''));\n" +
        "            var id = q.get('id') || h.get('id') || '';\n" +
        "            var sg = q.get('sg') || h.get('sg') || '';\n" +
        "            if (!id) return;\n" +
        "            try {\n" +
        "                _emit('STATUS', 'Fallback: Decrypting destination link...');\n" +
        "                const r = await fetch('https://go.urlking.in/api/d/p?id=' + encodeURIComponent(id) + '&_t=' + Date.now() + (sg ? '&token=' + encodeURIComponent(sg) : ''));\n" +
        "                const dp = await r.json().catch(() => ({}));\n" +
        "                if (dp && dp._v2_auth_sig) {\n" +
        "                    const gl = await fetch('https://go.urlking.in/api/v/get-link', {\n" +
        "                        method: 'POST',\n" +
        "                        headers: { 'Content-Type': 'application/json' },\n" +
        "                        body: JSON.stringify({ _v2_auth_sig: dp._v2_auth_sig, id: id, token: sg })\n" +
        "                    }).then(x => x.json()).catch(() => null);\n" +
        "                    if (gl && gl.success && gl.enc_d && gl._k_hash) {\n" +
        "                        const hx = s => new Uint8Array(s.match(/../g).map(x => parseInt(x, 16)));\n" +
        "                        const pa = gl.enc_d.split(':');\n" +
        "                        const k = await crypto.subtle.importKey('raw', hx(gl._k_hash), { name: 'AES-CBC' }, false, ['decrypt']);\n" +
        "                        const u = new TextDecoder().decode(await crypto.subtle.decrypt({ name: 'AES-CBC', iv: hx(pa[0]) }, k, hx(pa[1])));\n" +
        "                        if (u && u.indexOf('mediafire.com') !== -1) {\n" +
        "                            try {\n" +
        "                                fetch('https://go.urlking.in/api/click', {\n" +
        "                                    method: 'POST',\n" +
        "                                    headers: { 'Content-Type': 'application/json' },\n" +
        "                                    body: JSON.stringify({ id: id }),\n" +
        "                                    keepalive: true\n" +
        "                                });\n" +
        "                            } catch(e){}\n" +
        "                            notifyDestination(u);\n" +
        "                        }\n" +
        "                    }\n" +
        "                }\n" +
        "            } catch(e){}\n" +
        "        }, 12000);\n" +
        "    }\n" +
        "\n" +
        "    // ========================================================\n" +
        "    // STAGE B: go.urlking.in & urlking.in (Humanized 5-Step Dwell)\n" +
        "    // ========================================================\n" +
        "    var pathParts = location.pathname.split('/').filter(Boolean);\n" +
        "    var pathSlug = pathParts.length > 0 ? pathParts[pathParts.length - 1] : '';\n" +
        "    if (pathSlug === 'get' || pathSlug === 'blog' || pathSlug === 'best') pathSlug = '';\n" +
        "\n" +
        "    if (location.hostname.includes('urlking.in') && !location.hostname.includes('go.') && !location.hostname.includes('get.')) {\n" +
        "        if (pathSlug) {\n" +
        "            try {\n" +
        "                window.name = 'uk_id_' + pathSlug;\n" +
        "                sessionStorage.setItem('uk_safe_id', pathSlug);\n" +
        "                localStorage.setItem('urlking_active_id', pathSlug);\n" +
        "            } catch(e) {}\n" +
        "            location.replace('https://go.urlking.in/api/gate/' + pathSlug);\n" +
        "            return;\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    (async () => {\n" +
        "        if (location.hostname === 'get.urlking.in' || location.hostname.includes('mediafire.com')) return;\n" +
        "        const A = 'https://go.urlking.in',\n" +
        "              q = new URLSearchParams(location.search),\n" +
        "              h = new URLSearchParams(location.hash.replace(/^#/, '')),\n" +
        "              sl = m => new Promise(r => setTimeout(r, m)),\n" +
        "              D = t => {\n" +
        "                  try {\n" +
        "                      let p = String(t).split('.')[0].replace(/-/g, '+').replace(/_/g, '/');\n" +
        "                      p += '='.repeat((4 - p.length % 4) % 4);\n" +
        "                      const j = JSON.parse(atob(p));\n" +
        "                      return j && j.id ? j : null;\n" +
        "                  } catch(e) { return null; }\n" +
        "              },\n" +
        "              R = (s, k) => {\n" +
        "                  try {\n" +
        "                      const v = s.getItem(k);\n" +
        "                      return v ? JSON.parse(v) : null;\n" +
        "                  } catch(e) { return null; }\n" +
        "              },\n" +
        "              SV = s => {\n" +
        "                  try {\n" +
        "                      sessionStorage.setItem('uk_sg_session_v2', JSON.stringify(s));\n" +
        "                      localStorage.setItem('uk_sg_session_ls_v2', JSON.stringify(s));\n" +
        "                      localStorage.setItem('uk_p1_pass', String(Date.now()));\n" +
        "                  } catch(e) {}\n" +
        "              };\n" +
        "\n" +
        "        const M = t => {\n" +
        "            _emit('STATUS', t);\n" +
        "        };\n" +
        "\n" +
        "        const id = h.get('id') || q.get('id') || q.get('link_id') ||\n" +
        "                   (window.name && window.name.indexOf('uk_id_') === 0 ? window.name.slice(6) : '') ||\n" +
        "                   sessionStorage.getItem('uk_safe_id') ||\n" +
        "                   localStorage.getItem('urlking_active_id') ||\n" +
        "                   pathSlug || '';\n" +
        "\n" +
        "        if (!id) {\n" +
        "            M('Waiting for link id...');\n" +
        "            return;\n" +
        "        }\n" +
        "\n" +
        "        let tok = '', non = '', pl = null;\n" +
        "        const sg = q.get('sg') || h.get('sg') || '';\n" +
        "        pl = sg ? D(sg) : null;\n" +
        "        if (pl && pl.id === id && (!pl.exp || Date.now() < pl.exp)) {\n" +
        "            tok = sg;\n" +
        "            non = pl.r || '';\n" +
        "            SV({ token: tok, nonce: non });\n" +
        "            M('Resuming signed session...');\n" +
        "        }\n" +
        "        if (!tok) {\n" +
        "            const st = R(sessionStorage, 'uk_sg_session_v2') || R(localStorage, 'uk_sg_session_ls_v2');\n" +
        "            pl = st && st.token ? D(st.token) : null;\n" +
        "            if (pl && pl.id === id && (!pl.exp || Date.now() < pl.exp)) {\n" +
        "                tok = st.token;\n" +
        "                non = st.nonce || pl.r || '';\n" +
        "                M('Resuming stored session...');\n" +
        "            }\n" +
        "        }\n" +
        "\n" +
        "        const ST = async n => {\n" +
        "            try {\n" +
        "                const r = await fetch(A + '/api/sg/start', {\n" +
        "                    method: 'POST',\n" +
        "                    headers: { 'Content-Type': 'application/json' },\n" +
        "                    body: JSON.stringify({ id: String(id).slice(0, 64), steps: n })\n" +
        "                });\n" +
        "                if (r.status === 429) {\n" +
        "                    const d = await r.json().catch(() => ({}));\n" +
        "                    return { rate: d.retryAfterMs || 120000 };\n" +
        "                }\n" +
        "                if (!r.ok) return null;\n" +
        "                const d = await r.json();\n" +
        "                return d && d.token ? d : null;\n" +
        "            } catch(e) { return null; }\n" +
        "        };\n" +
        "\n" +
        "        const SP = async () => {\n" +
        "            try {\n" +
        "                const r = await fetch(A + '/api/sg/step', {\n" +
        "                    method: 'POST',\n" +
        "                    headers: { 'Content-Type': 'application/json', 'X-SG-Token': tok },\n" +
        "                    body: JSON.stringify({ token: tok, nonce: non })\n" +
        "                }), d = await r.json().catch(() => ({}));\n" +
        "                if (r.status === 429 && d.error === 'too_fast') return { wait: d.waitMs || 1500 };\n" +
        "                if (r.status === 429 && d.error === 'rate_limited') return { wait: d.retryAfterMs || 60000 };\n" +
        "                if (!r.ok || !d.token) return { dead: 1 };\n" +
        "                tok = d.token;\n" +
        "                non = d.nextNonce || (D(tok) || {}).r || '';\n" +
        "                SV({ token: tok, nonce: non });\n" +
        "                return { done: !(!d.done) };\n" +
        "            } catch(e) { return { dead: 1 }; }\n" +
        "        };\n" +
        "\n" +
        "        if (!tok) {\n" +
        "            M('Opening session...');\n" +
        "            let d = await ST(5);\n" +
        "            if (d && d.rate) {\n" +
        "                M('Rate limited. Retry in ' + Math.ceil(d.rate / 1000) + 's.');\n" +
        "                return;\n" +
        "            }\n" +
        "            if (!d || !d.token) d = await ST(8);\n" +
        "            if (!d || !d.token) {\n" +
        "                M('Start blocked. Open the short link page and run again.');\n" +
        "                return;\n" +
        "            }\n" +
        "            tok = d.token;\n" +
        "            non = d.nonce;\n" +
        "            SV({ token: tok, nonce: non });\n" +
        "        }\n" +
        "\n" +
        "        let rs = 0;\n" +
        "        for (let g = 0; g < 80; g++) {\n" +
        "            pl = D(tok) || {};\n" +
        "            const req = pl.req | 0, n = pl.n | 0;\n" +
        "            if (req && n >= req) break;\n" +
        "            M('Step ' + (n + 1) + ' / ' + (req || '?') + ' - clearing...');\n" +
        "\n" +
        "            const o = await SP();\n" +
        "            if (o.wait) {\n" +
        "                M('Server dwell ' + Math.ceil(o.wait / 1000) + 's...');\n" +
        "                await sl(o.wait + 200);\n" +
        "                continue;\n" +
        "            }\n" +
        "            if (o.dead) {\n" +
        "                if (rs++ > 0) {\n" +
        "                    M('Session dead. Reloading...');\n" +
        "                    return;\n" +
        "                }\n" +
        "                M('Restarting session...');\n" +
        "                let d = await ST(5);\n" +
        "                if (!d || !d.token) d = await ST(8);\n" +
        "                if (!d || !d.token) {\n" +
        "                    M('Restart failed.');\n" +
        "                    return;\n" +
        "                }\n" +
        "                tok = d.token;\n" +
        "                non = d.nonce;\n" +
        "                SV({ token: tok, nonce: non });\n" +
        "                continue;\n" +
        "            }\n" +
        "        }\n" +
        "\n" +
        "        pl = D(tok) || {};\n" +
        "        if (!pl.req || (pl.n | 0) < (pl.req | 0)) {\n" +
        "            M('Steps incomplete.');\n" +
        "            return;\n" +
        "        }\n" +
        "\n" +
        "        M('Steps cleared. Opening GET LINK page...');\n" +
        "        await sl(600);\n" +
        "        location.href = 'https://get.urlking.in/get?id=' + encodeURIComponent(id) + '&sg=' + encodeURIComponent(tok);\n" +
        "    })();\n" +
        "\n" +
        "    // MediaFire DOM Scanner\n" +
        "    function checkMediafire() {\n" +
        "        try {\n" +
        "            var h = (location.href || '').toLowerCase();\n" +
        "            if (h.indexOf('mediafire.com') !== -1) {\n" +
        "                notifyDestination(location.href);\n" +
        "                return true;\n" +
        "            }\n" +
        "            var mfLink = document.querySelector('a[href*=\"mediafire.com\"]');\n" +
        "            if (mfLink && mfLink.href) {\n" +
        "                notifyDestination(mfLink.href);\n" +
        "                return true;\n" +
        "            }\n" +
        "        } catch(e){}\n" +
        "        return false;\n" +
        "    }\n" +
        "\n" +
        "    checkMediafire();\n" +
        "    setInterval(checkMediafire, 500);\n" +
        "\n" +
        "    try {\n" +
        "        var observer = new MutationObserver(function() {\n" +
        "            checkMediafire();\n" +
        "        });\n" +
        "        if (document.body) {\n" +
        "            observer.observe(document.body, { childList: true, subtree: true, characterData: true });\n" +
        "        }\n" +
        "    } catch(e){}\n" +
        "})();\n";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isRotating = false;
    private boolean isMediafireTriggering = false;
    private boolean isAutoReloading = false;
    private boolean isResetting = false;
    private boolean isHandlingError = false;
    private boolean waitingForMediafire = false;
    private String pendingDestinationUrl = null;
    private String currentIp = "...";
    private String previousIp = "";

    // 240-Second Watchdog Timer (4 min: enough for all steps + cooldown)
    private final Runnable watchdogRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRotating && !isMediafireTriggering && !isResetting && !waitingForMediafire && pendingDestinationUrl == null) {
                mainHandler.post(() -> {
                    tvStatus.setTextColor(Color.parseColor("#FFA500"));
                    tvStatus.setText("Watchdog: timeout! Rotating IP...");
                    triggerRogueRedirectReset("timeout_240s");
                });
            }
        }
    };

    private void armWatchdog() {
        mainHandler.removeCallbacks(watchdogRunnable);
        mainHandler.postDelayed(watchdogRunnable, 240000);
    }

    private void disarmWatchdog() {
        mainHandler.removeCallbacks(watchdogRunnable);
    }

    private void handleJsBridgeEvent(String payload) {
        if (payload == null) return;
        if (payload.startsWith("DEST:")) {
            String destinationUrl = payload.substring("DEST:".length());
            if (!destinationUrl.toLowerCase().contains("mediafire.com")) return;
            mainHandler.post(() -> {
                waitingForMediafire = false;
                pendingDestinationUrl = destinationUrl;
                disarmWatchdog();
                tvStatus.setTextColor(Color.parseColor("#00E676"));
                tvStatus.setText("MediaFire Link Captured!");

                mainHandler.postDelayed(() -> {
                    if (webView != null && pendingDestinationUrl != null) {
                        tvStatus.setText("Opening MediaFire...");
                        webView.stopLoading();
                        webView.loadUrl(pendingDestinationUrl);
                    }
                }, 400);
            });
        } else if (payload.equals("GETLINK")) {
            mainHandler.post(() -> {
                waitingForMediafire = true;
                disarmWatchdog();
                tvStatus.setTextColor(Color.parseColor("#00E676"));
                tvStatus.setText("GET LINK clicked - waiting for MediaFire...");

                mainHandler.postDelayed(() -> {
                    if (waitingForMediafire) {
                        waitingForMediafire = false;
                        armWatchdog();
                    }
                }, 20000);
            });
        } else if (payload.startsWith("STATUS:")) {
            String status = payload.substring("STATUS:".length());
            mainHandler.post(() -> {
                if (!isRotating && !isMediafireTriggering && !isResetting) {
                    if (!waitingForMediafire) {
                        armWatchdog();
                    }
                    tvStatus.setTextColor(Color.parseColor("#00E676"));
                    tvStatus.setText(status);
                }
            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAirplane = findViewById(R.id.btn_airplane);
        btnReload = findViewById(R.id.btn_reload);
        tvStatus = findViewById(R.id.tv_status);
        tvIp = findViewById(R.id.tv_ip);
        progressBar = findViewById(R.id.progress_bar);
        webView = findViewById(R.id.webview);

        webView.setBackgroundColor(Color.WHITE);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Keep CPU awake when screen is locked
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UrlKing:WakeLock");
                wakeLock.acquire();
            }
        } catch (Exception ignored) {}

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Suppress X-Requested-With header so request headers match standalone Chrome
        if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
            WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, Collections.emptySet());
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null && consoleMessage.message() != null) {
                    String msg = consoleMessage.message();
                    if (msg.startsWith("__UK_BR__:")) {
                        handleJsBridgeEvent(msg.substring("__UK_BR__:".length()));
                        return true;
                    }
                }
                return super.onConsoleMessage(consoleMessage);
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }

                String curUrl = view.getUrl();
                if (newProgress >= 15 && curUrl != null && !curUrl.equals("about:blank")) {
                    view.evaluateJavascript(currentSpoofJs, null);
                    if (!curUrl.contains("mediafire.com")) {
                        view.evaluateJavascript(SCRIPT_URLKING, null);
                    }
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                result.confirm();
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url != null && !url.equals("about:blank")) {
                    view.evaluateJavascript(currentSpoofJs, null);
                    String lower = url.toLowerCase();
                    if (lower.contains("mediafire.com")) {
                        tvStatus.setTextColor(Color.parseColor("#00E676"));
                        tvStatus.setText("Loading MediaFire...");
                        return;
                    }
                    if (pendingDestinationUrl != null || isMediafireTriggering) {
                        return;
                    }
                    if (lower.contains("google.com/search") || lower.contains("google.co.in/search") || lower.contains("koekke.net")) {
                        triggerSearchGateReload();
                        return;
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                if (url == null || url.equals("about:blank")) return;

                view.evaluateJavascript(currentSpoofJs, null);
                if (url.toLowerCase().contains("mediafire.com")) {
                    checkAndHandleMediafire(url);
                    return;
                }
                view.evaluateJavascript(SCRIPT_URLKING, null);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame()) {
                    if (pendingDestinationUrl != null || isMediafireTriggering) {
                        return;
                    }
                    handleConnectionError();
                }
            }
        });

        btnAirplane.setOnClickListener(v -> startAirplaneReset());
        btnReload.setOnClickListener(v -> refreshIpAndLoad(true));

        checkRootAccess();
        startKeepAliveService();

        tvStatus.setTextColor(Color.parseColor("#00E676"));
        tvStatus.setText("Loading...");
        refreshIpAndLoad(true);
    }

    private void startKeepAliveService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
            ContextCompat.startForegroundService(this, new Intent(this, KeepAliveService.class));
        } catch (Exception ignored) {}
    }

    private boolean isKnownRogueAdUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        if (lower.contains("urlking.in") || lower.contains("mediafire.com") || lower.contains("google.")) {
            return false;
        }
        return lower.contains("omg10.com") ||
               lower.contains("popads") ||
               lower.contains("adsterra") ||
               lower.contains("bet365") ||
               lower.contains("1xbet") ||
               lower.contains("trafficjunky") ||
               lower.contains("exoclick") ||
               lower.contains("hilltopads") ||
               lower.contains("propellerads") ||
               lower.contains("clickadu") ||
               lower.contains("monetag") ||
               lower.contains("coinhive") ||
               lower.contains("doubleclick") ||
               lower.contains("adtrue") ||
               lower.contains("syndication") ||
               lower.contains("ey43.com") ||
               lower.contains("ouo.io") ||
               lower.contains("shrinkme.io");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.resumeTimers();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (webView != null) {
            webView.resumeTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        disarmWatchdog();
        if (isFinishing()) {
            try {
                stopService(new Intent(this, KeepAliveService.class));
            } catch (Exception ignored) {}
        }
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void handleConnectionError() {
        if (isHandlingError || isRotating) return;
        isHandlingError = true;

        mainHandler.post(() -> {
            tvStatus.setTextColor(Color.parseColor("#FFA500"));
            tvStatus.setText("Waiting for 4G/5G Data...");
        });

        new Thread(() -> {
            for (int i = 0; i < 40; i++) {
                try { Thread.sleep(700); } catch (InterruptedException ignored) {}
                if (isInternetAvailable()) {
                    break;
                }
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            mainHandler.post(() -> {
                isHandlingError = false;
                if (!isRotating && webView != null) {
                    tvStatus.setTextColor(Color.parseColor("#00E676"));
                    tvStatus.setText("Reloading...");
                    loadTargetWithTelegramReferer();
                }
            });
        }).start();
    }

    private void loadTargetWithTelegramReferer() {
        if (webView == null) return;
        Map<String, String> refHeader = new HashMap<>();
        refHeader.put("Referer", "https://t.me/");
        webView.loadUrl(TARGET_URL, refHeader);
    }

    private boolean handleUrl(WebView view, String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();

        if (lower.contains("mediafire.com")) {
            pendingDestinationUrl = url;
            tvStatus.setTextColor(Color.parseColor("#00E676"));
            tvStatus.setText("Opening MediaFire...");
            return false;
        }

        if (pendingDestinationUrl != null || isMediafireTriggering) {
            return true;
        }

        if (lower.contains("google.com/search") || lower.contains("google.co.in/search") || lower.contains("koekke.net")) {
            triggerSearchGateReload();
            return true;
        }

        if (isKnownRogueAdUrl(url)) {
            return true;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false;
        }
        return true;
    }

    // Search Gate Auto-Refresh (preserves uk_sg_session_v2 in WebStorage)
    private synchronized void triggerSearchGateReload() {
        if (isRotating || isAutoReloading || isResetting || isMediafireTriggering) return;
        isAutoReloading = true;

        mainHandler.post(() -> {
            tvStatus.setTextColor(Color.parseColor("#FFA500"));
            tvStatus.setText("Search Gate! Auto Refreshing...");
            Toast.makeText(MainActivity.this, "Search Gate Detected! Auto Refreshing...", Toast.LENGTH_SHORT).show();

            try {
                if (webView != null) {
                    webView.stopLoading();
                    loadTargetWithTelegramReferer();
                    armWatchdog();
                }
            } catch (Exception e) {
                softWipeSession();
                refreshIpAndLoad(true);
            }

            mainHandler.postDelayed(() -> isAutoReloading = false, 3000);
        });
    }

    // Rogue Redirect / Watchdog Reset
    private synchronized void triggerRogueRedirectReset(String rogueUrl) {
        if (isRotating || isMediafireTriggering || isResetting || waitingForMediafire) return;
        isResetting = true;
        disarmWatchdog();

        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    webView.stopLoading();
                    webView.loadUrl("about:blank");
                }
            } catch (Exception ignored) {}

            wipeSession();

            tvStatus.setTextColor(Color.parseColor("#FF5252"));
            tvStatus.setText("Resetting & Rotating IP...");
            Toast.makeText(MainActivity.this, "Resetting & Rotating IP...", Toast.LENGTH_SHORT).show();

            mainHandler.postDelayed(() -> {
                isResetting = false;
                startAirplaneReset();
            }, 1500);
        });
    }

    // MediaFire Detector - Lets MediaFire open, render, and dwell 1.5s before IP + device rotation
    private synchronized boolean checkAndHandleMediafire(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        if (lower.contains("mediafire.com")) {
            if (isRotating || isMediafireTriggering || isResetting) {
                return true;
            }
            isMediafireTriggering = true;
            disarmWatchdog();

            mainHandler.post(() -> {
                tvStatus.setTextColor(Color.parseColor("#00E676"));
                tvStatus.setText("MediaFire Page Loaded! (Dwell 1.5s)");
                Toast.makeText(MainActivity.this, "MediaFire Page Opened & Loaded!", Toast.LENGTH_SHORT).show();

                mainHandler.postDelayed(() -> {
                    wipeSession();
                    startAirplaneReset();
                }, 1500);
            });
            return true;
        }
        return false;
    }

    private void checkRootAccess() {
        new Thread(() -> {
            try {
                String pkg = getPackageName();
                runRootCommand("dumpsys deviceidle whitelist +" + pkg + "; cmd appops set " + pkg + " RUN_IN_BACKGROUND allow; cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow");
            } catch (Exception ignored) {}
        }).start();
    }

    private boolean isInternetAvailable() {
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL("https://connectivitycheck.gstatic.com/generate_204").openConnection());
            conn.setRequestProperty("User-Agent", "Android");
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.connect();
            return (conn.getResponseCode() == 204);
        } catch (Exception e) {
            return false;
        }
    }

    private void startAirplaneReset() {
        if (isRotating) return;
        isRotating = true;
        waitingForMediafire = false;
        pendingDestinationUrl = null;
        isResetting = false;
        isHandlingError = false;
        disarmWatchdog();

        mainHandler.post(() -> {
            setButtonsEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setTextColor(Color.parseColor("#FFA500"));
            tvStatus.setText("Airplane ON...");
        });

        new Thread(() -> {
            runRootCommand("cmd connectivity airplane-mode enable || settings put global airplane_mode_on 1");
            runRootCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");

            // Randomize Android ID + reset GMS Ad ID silently via root while radio is off
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    sb.append(Integer.toHexString(RANDOM.nextInt(16)));
                }
                String newAid = sb.toString();
                runRootCommand("settings put secure android_id " + newAid + "; content call --uri content://settings/secure --method PUT_secure --arg android_id --extra value:s:" + newAid + "; rm -f /data/data/com.google.android.gms/shared_prefs/adid_settings.xml");
            } catch (Exception ignored) {}

            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            mainHandler.post(() -> tvStatus.setText("Airplane OFF..."));
            runRootCommand("cmd connectivity airplane-mode disable || settings put global airplane_mode_on 0");
            runRootCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");

            mainHandler.post(() -> tvStatus.setText("Connecting 4G/5G..."));
            boolean connected = false;
            for (int i = 0; i < 45; i++) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                if (isInternetAvailable()) {
                    connected = true;
                    break;
                }
            }

            if (!connected) {
                mainHandler.post(() -> tvStatus.setText("Awaiting 4G/5G..."));
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            } else {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }

            // Active duplicate IP verification: retry up to 3 times if carrier assigns the same IP
            String freshIp = fetchPublicIp();
            for (int ipRetry = 1; ipRetry <= 3; ipRetry++) {
                if (previousIp.isEmpty() || previousIp.equals("...") || previousIp.equals("Connected") || previousIp.equals("Unknown") || !freshIp.equals(previousIp)) {
                    break;
                }
                final int retryNum = ipRetry;
                mainHandler.post(() -> tvStatus.setText("Same IP! Re-toggling Airplane (" + retryNum + "/3)..."));
                runRootCommand("cmd connectivity airplane-mode enable || settings put global airplane_mode_on 1");
                runRootCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
                try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                runRootCommand("cmd connectivity airplane-mode disable || settings put global airplane_mode_on 0");
                runRootCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
                for (int i = 0; i < 40; i++) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    if (isInternetAvailable()) break;
                }
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                freshIp = fetchPublicIp();
            }

            final boolean sameIpPersisted = !previousIp.isEmpty() && !previousIp.equals("...") &&
                !previousIp.equals("Connected") && !previousIp.equals("Unknown") && freshIp.equals(previousIp);
            previousIp = freshIp;
            final String finalIp = freshIp;

            mainHandler.post(() -> {
                currentIp = finalIp;
                tvIp.setText("IP: " + finalIp);
                Toast.makeText(MainActivity.this, sameIpPersisted ? "Same IP (cooling down 8s)..." : "New IP Active!", Toast.LENGTH_SHORT).show();
                tvStatus.setText(sameIpPersisted ? "Same IP! Cooling down 8s..." : "Fresh IP ready...");
            });

            // Only apply long cooldown when carrier returned the exact same IP; otherwise clear TCP pool briefly
            try { Thread.sleep(sameIpPersisted ? 8000 : 1200); } catch (InterruptedException ignored) {}

            mainHandler.post(() -> {
                isRotating = false;
                isMediafireTriggering = false;
                setButtonsEnabled(true);
                wipeSession();
                refreshIpAndLoad(true);
            });
        }).start();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnAirplane.setEnabled(enabled);
        btnReload.setEnabled(enabled);
    }

    private boolean runRootCommand(String command) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int exit = p.waitFor();
            return (exit == 0);
        } catch (Exception ignored) {
            return false;
        }
    }

    // Full wipe between completed/reset IP cycles so stale session tokens cannot bleed across devices/IPs
    private void wipeSession() {
        pendingDestinationUrl = null;
        try {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            WebStorage.getInstance().deleteAllData();
            if (webView != null) {
                webView.stopLoading();
                webView.clearCache(true);
                webView.clearHistory();
                webView.clearFormData();
                webView.clearSslPreferences();
            }
        } catch (Exception ignored) {}
    }

    // Soft wipe for mid-cycle reloads (preserves uk_sg_session_v2 in WebStorage)
    private void softWipeSession() {
        pendingDestinationUrl = null;
        try {
            CookieManager.getInstance().flush();
            if (webView != null) {
                webView.clearCache(true);
            }
        } catch (Exception ignored) {}
    }

    private void refreshIpAndLoad(boolean reloadWeb) {
        softWipeSession();
        armWatchdog();

        rotateDeviceProfile();
        try {
            if (webView != null) {
                webView.getSettings().setUserAgentString(activeProfile.ua);
            }
        } catch (Exception ignored) {
            String baseUa = WebSettings.getDefaultUserAgent(this);
            String cleanUa = baseUa.replace("; wv", "").replaceAll("Version/\\d+\\.\\d+\\s*", "");
            if (webView != null) {
                webView.getSettings().setUserAgentString(cleanUa);
            }
        }

        if (reloadWeb && webView != null) {
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setTextColor(Color.parseColor("#00E676"));
            tvStatus.setText("Loading...");
            loadTargetWithTelegramReferer();
        }

        new Thread(() -> {
            String ip = fetchPublicIp();
            mainHandler.post(() -> {
                currentIp = ip;
                tvIp.setText("IP: " + currentIp);
            });
        }).start();
    }

    private String fetchPublicIp() {
        String[] endpoints = {
            "https://api.ipify.org",
            "https://icanhazip.com",
            "https://checkip.amazonaws.com"
        };
        for (String ep : endpoints) {
            try {
                URL url = new URL(ep);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2500);
                conn.setReadTimeout(2500);
                if (conn.getResponseCode() == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String ip = r.readLine();
                    r.close();
                    if (ip != null && !ip.trim().isEmpty()) {
                        return ip.trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return "Connected";
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            moveTaskToBack(true);
        }
    }
}
