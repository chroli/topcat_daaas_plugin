

(function() {
    'use strict';

    var app = angular.module('topcat');
+
    app.directive('vnc', function(){
        return {
            restrict: 'E',
            scope: {
              machine: '=',
              height: '=',
              width: '=',
              changeResolution: '='
            },
            template: "<canvas></canvas>",
            controller: function($scope, $state, $element, $timeout, tc){
                $($element).css({
                    display: 'inline-block',
                    height: '100%',
                    width: '100%',
                });

                var username = tc.icat($state.params.facilityName).session().username;
                var machine = $scope.machine;
                var port = 29876;
                var path = "websockify?token=" + _.select(machine.users, function(user){ return user.userName == username})[0].websockifyToken;
                var width = getWidth();
                var height = getHeight();

                var noVnc = $($element).find('canvas').noVnc();
                noVnc.connect(machine.host, port, '', path);

                var interval;
                $($element).find('canvas').on('novnc:loaded', function(){
                    resize();
                    interval = setInterval(pollSize, 50);
                });

                $scope.$on('$destroy', function(){
                    clearInterval(interval);
                    noVnc.disconnect();
                });

                var changingResolution = false;
                function resize(){
                    if($scope.changeResolution){
                        if($($element).find('canvas').width() != width || $($element).find('canvas').height() != height){
                            changingResolution = true;
                            console.log('setResolution', width, height);
                            $timeout(function(){
                                machine.setResolution(width, height).then(function(){
                                    changingResolution = false;
                                });
                            });
                        }
                    } else {
                        noVnc.resize(width, height);
                    } 
                }

                function pollSize(){                    
                    if(!changingResolution){
                        var currentWidth = getWidth();
                        var currentHeight = getHeight();

                        console.log(currentWidth + 'x' + currentHeight);

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

