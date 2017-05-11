

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.directive('fileUpload', function(){
        return {
            restrict: 'E',
            scope: {
              prefix: '@'
            },
            template: '<span><input class="form-control" type="file"></span>',
            controller: function($scope, $element){
                var input = $($element).find('input')[0];
                var prefix = $scope.prefix;

                $(input).on('change', function(){
                    var file = input.files[0];
                    if(file){
                        var reader = new FileReader();

                        reader.onload = function(e){
                            console.log('e', e, reader.result);
                            eval("$scope.$parent." + prefix + 'MimeType' + " = file.type;");
                            console.log(reader.result);
                            eval("$scope.$parent." + prefix + 'Data' + " = new Uint8Array(reader.result);");
                        };

                        reader.readAsArrayBuffer(file);
                    } else {
                        eval("$scope.$parent." + prefix + 'MimeType' + " = '';");
                        eval("$scope.$parent." + prefix + 'Data' + " = new Uint8Array();");
                    }
                });
            }
        }
    });

})();

