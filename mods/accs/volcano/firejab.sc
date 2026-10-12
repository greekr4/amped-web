-1 : {
		findItem(0,-0.1,0,0.3,"finditem.sc");
	}
0 : {
		stop();
		fadeinAnimation( 230.0 , 230.0 , 230.0 , 4.5 , 0, 0.2) ;
	}
200 : {
		move(0,1);
		makeShapeModel("/effect/fire1.oa",-0.2,0.0,0.5,0,0,1);
		startAnimation( 230.0 , 231.0 , 231.0 , 10.0 , 0) ;
	}
300 : {
		attack("firejab");
	}
400 : {
		stop();
		nextTech();
	}
540 : {
		recover();
	}
