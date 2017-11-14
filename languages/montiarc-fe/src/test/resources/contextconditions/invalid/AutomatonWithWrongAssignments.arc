package contextconditions.invalid;

component AutomatonWithWrongAssignments{

port in Boolean i,
     out Boolean o;

automaton AutomatonWithWrongAssignments{

    state Idle, Computing;
    initial Idle;
    
    // No Error
    Idle -> Computing {i == true} / {o = false};
    // 2x Error stimulus has only one operator and o has two
    Idle -> Computing {i = true} / {o == false};
    // 1x Error stimulus has only one operator
    Idle -> Computing {i = true} / {o = false};
    // 1x Error reaction has two operators
    Idle -> Computing {i == true} / {o == false};
    
    
}
}