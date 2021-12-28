package com.tabnine.binary.fetch

import com.tabnine.general.StaticConfig.* // ktlint-disable no-wildcard-imports
import com.tabnine.testUtils.TestData.* // ktlint-disable no-wildcard-imports
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional
import java.util.stream.Collectors.toList
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class LocalBinaryVersionsTests {
    @TempDir
    @JvmField
    var temporaryFolder: Path? = null

    @MockK
    lateinit var binaryValidator: BinaryValidator

    @InjectMockKs
    lateinit var localBinaryVersions: LocalBinaryVersions

    private val originalHome = System.getProperty(USER_HOME_PATH_PROPERTY)

    @BeforeEach
    fun setup() {
        System.setProperty(USER_HOME_PATH_PROPERTY, temporaryFolder.toString())
    }

    @AfterEach
    fun teardown() {
        System.setProperty(USER_HOME_PATH_PROPERTY, originalHome)
    }

    @Test
    fun `given no versions in Tabnine binary folder when list existing then result is empty list`() {
        Paths.get(temporaryFolder!!.toFile().toString(), TABNINE_FOLDER_NAME).toFile().mkdirs()
        assertThat(localBinaryVersions.listExisting(), equalTo(emptyList<Any>()))
    }

    @Test
    fun `given some valid local versions when list existing versions then result contains sorted valid versions`() {
        every { binaryValidator.isWorking(versionFullPath(PRELATEST_VERSION)) } returns true
        every { binaryValidator.isWorking(versionFullPath(LATEST_VERSION)) } returns true
        every { binaryValidator.isWorking(versionFullPath(A_VERSION)) } returns true
        every { binaryValidator.isWorking(versionFullPath(ANOTHER_VERSION)) } returns false
        Stream.of(ANOTHER_VERSION, PRELATEST_VERSION, LATEST_VERSION, A_VERSION)
            .map { version: String? ->
                Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, version).toFile()
            }
            .forEach { obj: File -> obj.mkdirs() }

        assertThat(
            localBinaryVersions.listExisting(),
            equalTo(versions(LATEST_VERSION, PRELATEST_VERSION, A_VERSION))
        )
    }

    @Test
    fun `given all local versions invalid when list existing versions then result is empty list`() {
        every { binaryValidator.isWorking(any()) } returns false
        Stream.of(A_VERSION, ANOTHER_VERSION).map {
            Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, it).toFile()
        }.forEach { it.mkdirs() }

        assertThat(localBinaryVersions.listExisting(), empty())
    }

    @Test
    fun `given no active file then return none`() {
        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty<Any>()))
    }

    @Test
    fun `given active file with no lines then return none`() {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs()
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active").toFile().createNewFile()

        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty<Any>()))
    }

    @Test
    fun `given active file with invalid version then return none`() {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs()
        Files.write(
            Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active"),
            Stream.of(
                PREFERRED_VERSION
            ).collect(toList())
        )
        every { binaryValidator.isWorking(versionFullPath(PREFERRED_VERSION)) } returns false

        assertThat(localBinaryVersions.activeVersion(), equalTo(Optional.empty<Any>()))
    }

    @Test
    fun `given active file with valid version then return version`() {
        Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME).toFile().mkdirs()
        Files.write(
            Paths.get(temporaryFolder.toString(), TABNINE_FOLDER_NAME, ".active"),
            Stream.of(
                PREFERRED_VERSION
            ).collect(toList())
        )
        every { binaryValidator.isWorking(versionFullPath(PREFERRED_VERSION)) } returns true

        assertThat(
            localBinaryVersions.activeVersion(),
            equalTo(Optional.of(BinaryVersion(PREFERRED_VERSION)))
        )
    }
}
