package li.ktt.settings;

import com.intellij.openapi.options.ConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectSettingsPageTest {

    @Mock
    private ProjectSettings projectSettings;

    @Test
    public void shouldReturnCorrectPluginName() {
        // when
        ProjectSettingsPage page = new ProjectSettingsPage(projectSettings);

        // then
        assertEquals("DbUnit Extractor", page.getDisplayName());
        assertEquals("DbUnit Extractor", page.getId());
    }

    @Test
    public void shouldNotBeModify() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();
        // when // then
        assertEquals(false, page.isModified());
    }

    @Test
    public void shouldBeModifyIfIncludeSchemaHasChanged() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();
        // when
        page.getIncludeSchema().setSelected(false);
        // then
        assertEquals(true, page.isModified());
    }

    @Test
    public void shouldBeModifyIfSkipNullsHasChanged() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();
        // when
        page.getSkipNullValues().setSelected(true);
        // then
        assertEquals(true, page.isModified());
    }

    @Test
    public void shouldBeModifyIfSkipEmptyHasChanged() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();
        // when
        page.getSkipEmptyValues().setSelected(false);
        // then
        assertEquals(true, page.isModified());
    }

    @Test
    public void shouldBeModifyIfExcludedColumnsHasChanged() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();
        // when
        page.getExcludedColumns().setText("SomeNewVALUE");
        // then
        assertEquals(true, page.isModified());
    }

    @Test
    public void shouldReturnNullForUnusedElements() {
        // when
        ProjectSettingsPage page = new ProjectSettingsPage(projectSettings);

        // then
        assertEquals(null, page.getHelpTopic());
        assertEquals(null, page.enableSearch("ble"));
    }

    @Test
    public void shouldApplyConfiguration() throws ConfigurationException {
        // given
        ProjectSettingsPage page = preparePageWithMocks();

        // when
        page.getExcludedColumns().setText("newVALUE");
        page.getIncludeSchema().setSelected(false);
        page.getSkipNullValues().setSelected(true);
        page.apply();

        // then
        verify(projectSettings).setProperties(false, true, true, "newVALUE", null);
    }

    @Test
    public void shouldNotApplyConfiguration() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();

        // when
        page.getExcludedColumns().setText("newVALUE\\\n"
                + "someGoodValue\n"
                + "[\n"
                + "(\n"
                + ".*dbo\n");
        page.getIncludeSchema().setSelected(false);
        page.getSkipNullValues().setSelected(true);
        try {
            page.apply();
            fail("Should throw an exception");
        } catch (ConfigurationException e) {
            assertEquals("Invalid regular expressions in lines: 1, 3, 4", e.getMessage());
        }

        // then
        verify(projectSettings, times(0)).setProperties(anyBoolean(), anyBoolean(), anyBoolean(), anyString(), anyString());
    }

    @Test
    public void shouldResetConfiguration() {
        // given
        ProjectSettingsPage page = preparePageWithMocks();

        // when
        page.getExcludedColumns().setText("newVALUE\\\n"
                + "someGoodValue\n"
                + "[\n"
                + "(\n"
                + ".*dbo\n");
        page.getIncludeSchema().setSelected(false);
        page.getSkipNullValues().setSelected(true);

        page.reset();

        // then
        assertEquals(true, page.getIncludeSchema().isSelected());
        assertEquals(false, page.getSkipNullValues().isSelected());
        assertEquals(true, page.getSkipEmptyValues().isSelected());
        assertEquals("ble\\.value\n", page.getExcludedColumns().getText());
    }

    private ProjectSettingsPage preparePageWithMocks() {
        ExtractorProperties extractorProperties = new ExtractorProperties(true, false, true, "ble\\.value\n", null);
        when(projectSettings.getExtractorProperties()).thenReturn(extractorProperties);
        ProjectSettingsPage page = new ProjectSettingsPage(projectSettings);
        page.createComponent();
        return page;
    }
}
