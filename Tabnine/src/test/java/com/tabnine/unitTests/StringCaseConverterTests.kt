package com.tabnine.unitTests

import com.tabnineCommon.chat.commandHandlers.utils.StringCaseConverter
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class StringCaseConverterTests {
    @Test
    fun toCamelCase() {
        assert(StringCaseConverter.toCamelCase("") == "")
        assert(StringCaseConverter.toCamelCase("kaki_pipi") == "kakiPipi")
        assert(StringCaseConverter.toCamelCase("kaki") == "kaki")
    }

    @Test
    fun toSnakeCase() {
        assert(StringCaseConverter.toSnakeCase("") == "")
        assert(StringCaseConverter.toSnakeCase("kakiPipi") == "kaki_pipi")
        assert(StringCaseConverter.toSnakeCase("KakiPipi") == "kaki_pipi")
        assert(StringCaseConverter.toSnakeCase("Kaki") == "kaki")
        assert(StringCaseConverter.toSnakeCase("kaki") == "kaki")
    }

    @Test
    fun shouldNotChangeSameFormatSnakeCase() {
        assert(StringCaseConverter.toSnakeCase("") == "")
        assert(StringCaseConverter.toSnakeCase("kaki_pipi") == "kaki_pipi")
        assert(StringCaseConverter.toCamelCase("kaki") == "kaki")
    }

    @Test
    fun shouldNotChangeSameFormatCamelCase() {
        assert(StringCaseConverter.toCamelCase("") == "")
        assert(StringCaseConverter.toCamelCase("kakiPipi") == "kakiPipi")
        assert(StringCaseConverter.toCamelCase("KakiPipi") == "KakiPipi")
        assert(StringCaseConverter.toCamelCase("kaki") == "kaki")
    }
}
