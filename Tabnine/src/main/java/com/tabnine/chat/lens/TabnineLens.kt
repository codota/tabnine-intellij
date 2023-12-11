package com.tabnine.chat.lens

import com.tabnine.chat.ChatEnabledState
import com.tabnineCommon.chat.lens.TabnineLensJavaBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensKotlinBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensPhpBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensPythonBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensRustBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensTypescriptBaseProvider

open class TabnineLensJavaProvider : TabnineLensJavaBaseProvider({ ChatEnabledState.instance.get().enabled })
open class TabnineLensPythonProvider : TabnineLensPythonBaseProvider({ ChatEnabledState.instance.get().enabled })
open class TabnineLensTypescriptProvider : TabnineLensTypescriptBaseProvider({ ChatEnabledState.instance.get().enabled })
open class TabnineLensKotlinProvider : TabnineLensKotlinBaseProvider({ ChatEnabledState.instance.get().enabled })
open class TabnineLensPhpProvider : TabnineLensPhpBaseProvider({ ChatEnabledState.instance.get().enabled })
open class TabnineLensRustProvider : TabnineLensRustBaseProvider({ ChatEnabledState.instance.get().enabled })
