var rest = require('rest');
var defaultRequest = require('rest/interceptor/defaultRequest');
var mime = require('rest/interceptor/mime');

var uriTemplateInterceptor = () => {
    var interceptor = require('rest/interceptor');

    return interceptor({
        request: function (request /*, config, meta */) {
            /* If the URI is a URI Template per RFC 6570 (http://tools.ietf.org/html/rfc6570), trim out the template part */
            if (request.path.indexOf('{') === -1) {
                return request;
            } else {
                request.path = request.path.split('{')[0];
                return request;
            }
        }
    });
}
var uriListConverter = {
    read: function(str /*, opts */) {
        return str.split('\n');
    },
    write: function(obj /*, opts */) {
        // If this is an Array, extract the self URI and then join using a newline
        if (obj instanceof Array) {
            return obj.map(function(resource) {
                return resource._links.self.href;
            }).join('\n');
        } else { // otherwise, just return the self URI
            return obj._links.self.href;
        }
    }
}

var errorCode = require('rest/interceptor/errorCode');
var baseRegistry = require('rest/mime/registry');

var registry = baseRegistry.child();

registry.register('text/uri-list', uriListConverter);
registry.register('application/hal+json', require('rest/mime/type/application/hal'));
registry.register('rest/interceptor/jsonp', require('rest/client/jsonp'));

module.exports = rest
    .wrap(mime, { registry: registry })
    .wrap(uriTemplateInterceptor())
    .wrap(errorCode)
.wrap(defaultRequest, { headers: { 'Accept': 'application/hal+json' }});