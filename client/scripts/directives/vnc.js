

(function() {
    'use strict';

    var app = angular.module('topcat');
+
    app.directive('vnc', function(){
        return {
            restrict: 'E',
            scope: {
              token: '=',
              height: '=',
              width: '=',
              changeResolution: '='
            },
            template: "<canvas></canvas>",
            controller: function($scope, $element, $timeout){
                $($element).css({
                    display: 'inline-block',
                    height: '100%',
                    width: '100%'
                });

                var host = window.location.hostname;
                var port = 29876;
                var path = "websockify?token=" + $scope.token;
                var width = getWidth();
                var height = getHeight();

                var noVnc = $($element).find('canvas').noVnc();
                noVnc.connect(host, port, '', path);

                var interval;
                $($element).find('canvas').on('novnc:loaded', function(){
                    resize();
                    interval = setInterval(pollSize, 50);
                });

                $scope.$on('$destroy', function(){
                    clearInterval(interval);
                    noVnc.disconnect();
                });

                function resize(){
                    if($scope.changeResolution){
                        if($($element).find('canvas').width() != width || $($element).find('canvas').height() != height){
                            changeResolution(width, height);
                        }
                    } else {
                        noVnc.resize(width, height);
                    } 
                }

                var changingResolution = false;
                function changeResolution(width, height){
                    if(!changingResolution){
                        console.log('changeResolution', width, height);
                        changingResolution = true;
                        noVnc.sendKey('Alt_L', true);
                        noVnc.sendKey('8');
                        noVnc.sendKey('Alt_L', false);

                        var chars = (width + "x" + height).split('');
                        $timeout(function() {
                            _.each(chars, function(char){
                                noVnc.sendKey(char); 
                            });
                            noVnc.sendKey('KP_Enter');
                        }, 500);
                    }
                }

                $($element).find('canvas').on('novnc:resolutionchanged', function(){
                    changingResolution = false;
                    console.log('novnc:resolutionchanged');
                });

                function pollSize(){                    
                    if(!changingResolution){
                        var currentWidth = getWidth();
                        var currentHeight = getHeight();

                        if(currentWidth != width || currentHeight != height){
                            width = currentWidth;
                            height = currentHeight;
                            resize();
                        }
                    }
                }

                function getWidth(){
                    return $scope.width || $($element).parent().width();
                }

                function getHeight(){
                    return $scope.height || $($element).parent().height();
                }

            }
        }
    });

})();

