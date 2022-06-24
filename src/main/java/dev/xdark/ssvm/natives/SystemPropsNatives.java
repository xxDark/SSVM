package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * Initializes jdk/internal/util/SystemProps.
 *
 * @author xDark
 */
@UtilityClass
public class SystemPropsNatives {

	private final int _display_country_NDX = 0;
	private final int _display_language_NDX = 1 + _display_country_NDX;
	private final int _display_script_NDX = 1 + _display_language_NDX;
	private final int _display_variant_NDX = 1 + _display_script_NDX;
	private final int _file_encoding_NDX = 1 + _display_variant_NDX;
	private final int _file_separator_NDX = 1 + _file_encoding_NDX;
	private final int _format_country_NDX = 1 + _file_separator_NDX;
	private final int _format_language_NDX = 1 + _format_country_NDX;
	private final int _format_script_NDX = 1 + _format_language_NDX;
	private final int _format_variant_NDX = 1 + _format_script_NDX;
	private final int _ftp_nonProxyHosts_NDX = 1 + _format_variant_NDX;
	private final int _ftp_proxyHost_NDX = 1 + _ftp_nonProxyHosts_NDX;
	private final int _ftp_proxyPort_NDX = 1 + _ftp_proxyHost_NDX;
	private final int _http_nonProxyHosts_NDX = 1 + _ftp_proxyPort_NDX;
	private final int _http_proxyHost_NDX = 1 + _http_nonProxyHosts_NDX;
	private final int _http_proxyPort_NDX = 1 + _http_proxyHost_NDX;
	private final int _https_proxyHost_NDX = 1 + _http_proxyPort_NDX;
	private final int _https_proxyPort_NDX = 1 + _https_proxyHost_NDX;
	private final int _java_io_tmpdir_NDX = 1 + _https_proxyPort_NDX;
	private final int _line_separator_NDX = 1 + _java_io_tmpdir_NDX;
	private final int _os_arch_NDX = 1 + _line_separator_NDX;
	private final int _os_name_NDX = 1 + _os_arch_NDX;
	private final int _os_version_NDX = 1 + _os_name_NDX;
	private final int _path_separator_NDX = 1 + _os_version_NDX;
	private final int _socksNonProxyHosts_NDX = 1 + _path_separator_NDX;
	private final int _socksProxyHost_NDX = 1 + _socksNonProxyHosts_NDX;
	private final int _socksProxyPort_NDX = 1 + _socksProxyHost_NDX;
	private final int _sun_arch_abi_NDX = 1 + _socksProxyPort_NDX;
	private final int _sun_arch_data_model_NDX = 1 + _sun_arch_abi_NDX;
	private final int _sun_cpu_endian_NDX = 1 + _sun_arch_data_model_NDX;
	private final int _sun_cpu_isalist_NDX = 1 + _sun_cpu_endian_NDX;
	private final int _sun_io_unicode_encoding_NDX = 1 + _sun_cpu_isalist_NDX;
	private final int _sun_jnu_encoding_NDX = 1 + _sun_io_unicode_encoding_NDX;
	private final int _sun_os_patch_level_NDX = 1 + _sun_jnu_encoding_NDX;
	private final int _sun_stderr_encoding_NDX = 1 + _sun_os_patch_level_NDX;
	private final int _sun_stdout_encoding_NDX = 1 + _sun_stderr_encoding_NDX;
	private final int _user_dir_NDX = 1 + _sun_stdout_encoding_NDX;
	private final int _user_home_NDX = 1 + _user_dir_NDX;
	private final int _user_name_NDX = 1 + _user_home_NDX;
	private final int FIXED_LENGTH = 1 + _user_name_NDX;

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/util/SystemProps$Raw");
		if (jc != null) {
			VMInterface vmi = vm.getInterface();
			vmi.setInvoker(jc, "platformProperties", "()[Ljava/lang/String;", ctx -> {
				ctx.setResult(vm.getHelper().newArray(vm.getSymbols().java_lang_String(), FIXED_LENGTH));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "vmProperties", "()[Ljava/lang/String;", ctx -> {
				Map<String, String> properties = vm.getProperties();
				VMHelper helper = vm.getHelper();
				ArrayValue array = helper.newArray(vm.getSymbols().java_lang_String(), properties.size() * 2);
				int i = 0;
				for (Map.Entry<String, String> entry : properties.entrySet()) {
					array.setValue(i++, helper.newUtf8(entry.getKey()));
					array.setValue(i++, helper.newUtf8(entry.getValue()));
				}
				ctx.setResult(array);
				return Result.ABORT;
			});
		}
	}
}
