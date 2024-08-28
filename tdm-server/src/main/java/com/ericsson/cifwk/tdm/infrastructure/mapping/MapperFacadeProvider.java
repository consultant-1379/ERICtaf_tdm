package com.ericsson.cifwk.tdm.infrastructure.mapping;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.ContextRole;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.model.ContextEntity;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.ericsson.gic.tms.presentation.dto.users.GrantedUserRole;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperFacadeProvider {

    @Bean(name = "mapperFactory")
    public MapperFactory mapperFactory() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.getConverterFactory().registerConverter(new DataSourcesToGroupsConverter());
        mapperFactory.classMap(ContextBean.class, ContextEntity.class)
                .field("id", "systemId")
                .byDefault()
                .register();
        mapperFactory.classMap(ContextEntity.class, Context.class)
                .field("systemId", "id")
                .byDefault()
                .register();

        mapperFactory.classMap(UserBean.class, User.class)
            .exclude("canonical")
            .exclude("roles")
            .byDefault()
            .register();

        mapperFactory.classMap(GrantedUserRole.class, ContextRole.class)
            .field("roleBean.name", "role")
            .byDefault()
            .register();

        return mapperFactory;
    }

    @Bean(name = "mapperFacade")
    public MapperFacade mapperFacade() {
        return mapperFactory().getMapperFacade();
    }
}
