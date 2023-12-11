package com.tabnineSelfHosted.chat.lens

import com.tabnineCommon.chat.lens.TabnineLensJavaBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensKotlinBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensPhpBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensPythonBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensRustBaseProvider
import com.tabnineCommon.chat.lens.TabnineLensTypescriptBaseProvider
import com.tabnineSelfHosted.chat.SelfHostedChatEnabledState

open class TabnineLensJavaProvider : TabnineLensJavaBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
open class TabnineLensPythonProvider : TabnineLensPythonBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
open class TabnineLensTypescriptProvider : TabnineLensTypescriptBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
open class TabnineLensKotlinProvider : TabnineLensKotlinBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
open class TabnineLensPhpProvider : TabnineLensPhpBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
open class TabnineLensRustProvider : TabnineLensRustBaseProvider({ SelfHostedChatEnabledState.instance.get().enabled })
