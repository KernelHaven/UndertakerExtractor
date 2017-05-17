
#ifdef CONFIG_A

#endif

#if defined(CONFIG_B) \
|| !(CONFIG_C)

#else

    #if CONFIG_A
    
    #elif CONFIG_B
    
    #else
    
    #endif

#endif
