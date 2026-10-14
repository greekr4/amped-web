0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.2) ;
		makeShapeModel("/effect/flash.oa",0,0.4,-0.3,0,1,0);
		makeShapeModel("/effect/elec.oa",0,0.3,-0.3,0,1,0);
	}
300 : {
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
	}
380 : {
		attack("droneL");
		makeShapeModel("/effect/spark.oa",0.3,0.2,0.5,0,0,1);
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
580 : {
		attack("droneR");
		makeShapeModel("/effect/spark.oa",-0.3,0.2,0.5,0,0,1);
	}
900 : {
		nextTech();
	}
1200 : {
		recover();
	}
