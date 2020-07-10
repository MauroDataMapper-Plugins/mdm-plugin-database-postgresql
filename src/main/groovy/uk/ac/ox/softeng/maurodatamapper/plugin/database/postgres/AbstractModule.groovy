package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import com.google.common.base.Strings

/**
 * @since 17/08/2017
 */
abstract class AbstractModule extends Module {
    @Override
    String getVersion() {
        String version = getClass().getPackage().getSpecificationVersion()
        return Strings.isNullOrEmpty(version) ? "unknown" : version
    }

    @Override
    Closure doWithSpring() {
        return null
    }
}
