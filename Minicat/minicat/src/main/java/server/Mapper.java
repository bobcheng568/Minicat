package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author bobcheng
 * @date 2020/5/6
 */
public class Mapper {

    List<MappedHost> hosts = new ArrayList<>();


    public void addHost(String name, Host host) {
        hosts.add(new MappedHost(name, host));
    }

    public void addContext(String hostName, String name, Context context) {
        hosts.stream()
                .filter(host -> hostName.equals(host.name))
                .forEach(host -> host.contexts.add(new MappedContext(name, context)));
    }

    public void addWrapper(String hostName, String contextName, String name, Wrapper wrapper) {
        hosts.stream()
                .filter(host -> hostName.equals(host.name))
                .flatMap(host -> host.contexts.stream())
                .filter(context -> contextName.equals(context.name))
                .forEach(context -> context.exactWrappers.add(new MappedWrapper(name, wrapper)));
    }

    public Wrapper getWrapper(Request request) {
        Optional<MappedWrapper> optional = hosts.stream()
                .filter(host -> host.name.equals(request.getHost()))
                .flatMap(host -> host.contexts.stream())
                .filter(context -> context.name.equals(request.getContext()))
                .flatMap(context -> context.exactWrappers.stream())
                .filter(exactWrapper -> exactWrapper.name.equals(request.getUrl()))
                .findFirst();
        return optional.map(mappedWrapper -> mappedWrapper.object).orElse(null);
    }

    protected abstract static class MapElement<T> {
        public final String name;
        public final T object;

        public MapElement(String name, T object) {
            this.name = name;
            this.object = object;
        }
    }


    protected static final class MappedHost extends MapElement<Host> {

        List<MappedContext> contexts = new ArrayList<>();

        public MappedHost(String name, Host host) {
            super(name, host);
        }
    }



    protected static final class MappedContext extends MapElement<Context> {

        List<MappedWrapper> exactWrappers = new ArrayList<>();

        public MappedContext(String name, Context context) {
            super(name, context);
        }
    }


    protected static class MappedWrapper extends MapElement<Wrapper> {

        public MappedWrapper(String name, Wrapper wrapper) {
            super(name, wrapper);
        }
    }


}
