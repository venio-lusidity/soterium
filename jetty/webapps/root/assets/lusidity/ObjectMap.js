 /**
 * All keys are case sensitive so "Chevy" and "chevy" are two different keys.
 * @param keyType 
 *  The expected type of the key. 'string' or 'number'
 * @param valueType
 *  The expected type of the value.
 * @param overwrite
 *  If set to true and a key exists it will be deleted and then added again.
 *  Default value is true.
 *  @param grouped
 *  overwrite must be set to false, otherwise this parameter is never used.
 *  If set to true and overwrite is set to false.
 *          1. The entry will be retrieved/created by key and the value will be added to an array.
 *          2. Using map.remove(key) will remove the entire entire and all values.
 *          3. Using map.remove(key, index) will retrieve the entry and remove the value at the specified index.
 *  If set to false and overwrite is set to false.
 *          1. duplicate keys will be allowed.
 *          2. Using map.remove(key) will result in all matching keys will be removed, it is recommended to use map.removeAt(index);
 *  Default value is false.
 * @constructor
 */
var ObjectMap = function(keyType, valueType, overwrite, grouped){
    if(null===overwrite || undefined===overwrite){
        overwrite = true;
    }
    if(null===grouped || undefined===grouped){
        grouped = false;
    }
    var properties = {                                                 
        keyType: keyType,
        valueType: valueType,
        underlying: [],
        overwrite: overwrite,
        grouped: grouped
    };

    var methods = {
        getEntry: function (key) {
            var result;
            methods.each(function(entry, index){
                if (entry.key === key) {
                    result = entry;
                    return false;
                }
            });
            return result;
        },
        getEntryAt: function (idx) {
            var result;
            methods.each(function(entry, index){
                if(idx===index){
                    result = entry;
                    return false;
                }
            });
            return result;
        },
        getValue: function (key) {
            var entry = methods.getEntry(key);
            return (null !== entry && undefined !== entry) ? entry.value : null;
        },
        getValueAt: function (index) {
            var entry = methods.getEntryAt(index);
            return (null !== entry && undefined !== entry) ? entry.value : null;
        },
        hasKey: function(key){
            var result = false;
            methods.each(function(entry, index){
                result = (entry.key===key);
                return !result;
            });
            return result;
        },
        put: function (key, value) {
            try {
                if (!(typeof(key) === properties.keyType)) {
                    throw 'The key is not the expected type, ' + properties.keyType;
                }

                if (!(typeof(value) === properties.valueType)) {
                    throw 'The value is not the expected type, ' + properties.keyType;
                }

                var entry;

                if (properties.overwrite) {
                    if (properties.overwrite && methods.hasKey(key)) {
                        entry = methods.getEntry(key);
                        entry.value = value;
                    }
                    else {
                        entry = {key: key, value: value};
                        properties.underlying.push(entry);
                    }
                }
                else if (properties.grouped) {
                    if (!methods.hasKey(key)) {
                        entry = {key: key, value: []};
                        properties.underlying.push(entry);
                    }
                    else {
                        entry = methods.getEntry(key);
                    }

                    if (null !== entry && undefined !== entry
                        && null !== value && undefined !== value) {
                        entry.value.push(value);
                    }
                }
                else {
                    entry = {key: key, value: value};
                    properties.underlying.push(entry);
                }
            }catch (e){}
        },
        remove: function (key) {
            var temp = [];
            methods.each(function (entry, index) {
                if (entry.key !== key) {
                    temp.push(entry);
                }
            });
            properties.underlying = temp;
        },
        removeAt: function (idx) {
            var temp = [];
            methods.each(function (entry, index) {
                if (idx !== index) {
                    temp.push(entry);
                }
            });
            properties.underlying = temp;
        },
        removeValue: function(key, idx){
            if(!properties.overwrite && methods.hasKey(key)){
                var entry = methods.getEntry(key);
                var temp = [];
                var on=0;
                var len = entry.value.length;
                for(var i=0;i<len;i++){
                    if(i!==idx){
                        var value = entry.value[i];
                        temp.push(value);
                    }
                }
                entry.value = temp;
            }
        },
        sort: function(asc) {
            function lowerCase(obj){
                if(typeof(obj)==='string'){
                    obj = obj.toLowerCase();
                }
                return obj;
            }
            properties.underlying.sort(function(a, b){
                var aKey = lowerCase(a.key);
                var bKey = lowerCase(b.key);
                return ((aKey > bKey) ? 1 : (aKey< bKey) ? -1 : 0) * (asc ? 1 : -1);
            });
        },
        each: function(fn /*function*/){
            if(typeof(fn) === 'function') {
                var len = properties.underlying.length;
                for (var index = 0; index < len; index++) {
                    var entry = properties.underlying[index];
                    var result = fn(entry, index);
                    if(null!==result && undefined!==result && !result){
                        break;
                    }
                }
            }
        }
    };

    /**
     * Iterate all the entries.
     * @param fn
     *  A function to execute on each entry.
     *  @remarks To stop iterating return false in the function.
     */
    this.each = function(fn /*function*/){
        methods.each(fn);
    };

    /**
     * Get all entries.
     * @returns {*}
     */
    this.getEntries = function(){
        return properties.underlying;
    };

    /**
     * Get an entry by its key.
     * @param key
     *  The key of the entry.
     * @remarks Will only return the first instance of the key.
     */
    this.getEntry = function(key){
        return methods.getEntry(key);
    };

    /**
     * Get an entry at the specified index.
     * @param index
     *  The specified index.
     * @returns An entry.
     */
    this.getEntryAt = function(index){
        return methods.getEntryAt(index);
    };

    /**
     * Get a value by the specified key.
     * @param key
     *  The key of an entry.
     * @returns The value of an entry.
     */
    this.getValue = function(key){
        return methods.getValue(key);
    };

    /**
     * Get an entry at the specified index, returning only the value.
     * @param index The index of the entry.
     * @returns The value of an entry.
     */
    this.getValueAt = function(index){
       return methods.getValueAt(index);
    };

    /**
     *  Does this have the specified key?
     * @param key
     *  The key to check for.
     * @returns true if the ObjectMap has the specified key.
     */
    this.hasKey = function(key){
        return methods.hasKey(key);
    };

    /**
     * Add or insert an entry.
     * @param key
     *  The key of the entry.
     * @param value
     *  The value of the entry.
     * @remarks The value of “value” can be null.
     */
    this.put = function(key, value){
        methods.put(key, value);
    };

    /**
     * Remove a value by the specified key.
     * @param key
     *  The key of an entry.
     * @remarks All entries in the map that have the specified key will be removed.
     */
    this.remove = function(key){
        methods.remove(key);
    };

    /**
     * Remove an entry at the specified index.
     * @param index
     *  The index of the entry.
     */
    this.removeAt = function(index){
        methods.removeAt(index);
    };

    /**
     * Get an entry by the specified id and remove a value by the specified index.
     * @param key
     *  The key of an entry.
     * @param index
     *  The index of the value to remove.
     * @remarks Only works on a group ObjectMap.
     */
    this.removeValue = function(key, index){
         methods.removeValue(key, index);
    };

    /**
     * Sort the map.
     * @param asc
     *  If true the map keys will be sorted ascending otherwise descending.
     * @remarks Default sort is not sorted.
     */
    this.sort = function(asc){
        methods.sort(asc);
    };

    /**
     * The size of the ObjectMap.
     * @returns the size of the ObjectMap.
     */
    this.size = function(){
        return this.getEntries().length;
    };

    /**
     * Is the ObjectMap empty?
     * @returns true if the ObjectMap size equals zero.
     */
    this.isEmpty = function(){
        return (this.size()===0);
    };

    this.test = {
        all: function(){
            this.expectedTypes();
            this.each();
            this.overwrite();
            this.grouped();
            this.duplicates();
            this.remove();
            this.removeAt();
            this.removeValue();
            this.sort();
        },
        _data: function(map){
            map.put('Chevy', '2015 Camaro');
            map.put('Chevy', '2015 Camaro Convertible');
            map.put('Chevy', '2015 Camaro ZL1');
            map.put('chevy', '2015 Camaro ZL1 Convertible');
        },
        expectedTypes: function(){
            console.log('****************** expectedTypes test started ******************');
            var passed = true;
            var map = new ObjectMap('string', 'string');

            try{
                console.log('You should see an error about the key.');
                map.put(1, 'value1');
                passed = false;
            }
            catch(e){
                console.log(e);
            }

            try{
                console.log('You should see an error about the key.');
                map.put(1, 'value1');
                passed = false;
            }
            catch(e){
                console.log(e);
            }

            assert.passed('test.expectedTypes', passed);
        },
        overwrite: function(){
            console.log('****************** overwrite test started ******************');
            var passed = true;
            var map = new ObjectMap('string', 'string', true, false);

            this._data(map);

            passed =  map.getEntries().length===2;

            console.log(map.getEntries());
            assert.passed('test.overwrite', passed);
        },
        grouped: function(){
            console.log('****************** grouped test started ******************');

            var passed = true;
            var map = new ObjectMap('string', 'string', false, true);

            this._data(map);
            
            var expected1 = map.getEntry('Chevy');
            var expected2 = map.getEntry('chevy');

            passed  =  map.getEntries().length===2 && expected2.value.length===1 && expected1.value.length===3;

            console.log(map.getEntries());
            assert.passed('test.grouped', passed);
        },
        duplicates: function(){
            console.log('****************** duplicates test started ******************');

            var passed = true;
            var map = new ObjectMap('string', 'string', false, false);

            this._data(map);

            passed  =  map.getEntries().length===4;

            console.log(map.getEntries());
            assert.passed('test.duplicates', passed);
        },
        remove: function(){
            console.log('****************** remove test started ******************');
            var passed = true;
            var map = new ObjectMap('string', 'string', true, false);

            map.put('Chevy1', '2015 Camaro');
            map.put('Chevy2', '2015 Camaro Convertible');
            map.put('Chevy3', '2015 Camaro ZL1');
            map.put('chevy4', '2015 Camaro ZL1 Convertible');

            map.remove('Chevy1');

            assert.areEqual('There should have been 3 items left, found ' + map.length, 3, map.getEntries().length);

            map.remove('chevy4');

            assert.areEqual('There should have be 2 items left, found ' + map.length, 2, map.getEntries().length);

            passed =  map.getEntries().length===2;

            console.log(map.getEntries());
            assert.passed('test.remove', passed);
        },
        removeAt: function(){
            console.log('****************** removeAt test started ******************');
            var passed = true;
            var map = new ObjectMap('string', 'string', true, false);

            map.put('Chevy1', '2015 Camaro');
            map.put('Chevy2', '2015 Camaro Convertible');
            map.put('Chevy3', '2015 Camaro ZL1');
            map.put('chevy4', '2015 Camaro ZL1 Convertible');

            map.removeAt(0);

            assert.areEqual('There should have been 3 items left, found ' + map.length, 3, map.getEntries().length);

            map.removeAt(1);

            assert.areEqual('There should have be 2 items left, found ' + map.length, 2, map.getEntries().length);

            passed =  map.getEntries().length===2;

            console.log(map.getEntries());
            assert.passed('test.removeAt', passed);
        },
        removeValue: function(){
            console.log('****************** removeValue test started ******************');
            var passed = true;
            var map = new ObjectMap('string', 'string', false, true);

            this._data(map);

            map.removeValue('Chevy', 2);

            var entry = map.getEntry('Chevy');

            passed = assert.areEqual('There should have been 2 values left, found ' + entry.value.length, 2, entry.value.length);

            map.removeValue('chevy', 0);
            var entry2 = map.getEntry('chevy');

            passed = assert.areEqual('There should have be 0 items left, found ' + entry2.value.length, 0, entry2.value.length);

            console.log(map.getEntries());
            assert.passed('test.removeValue', passed);
        },
        sort: function(){
            console.log('****************** sort test started ******************');
            var passed = true;
            var map = new ObjectMap('number', 'string', true, false);

            for(var i=0;i<50;i++) {
                map.put(i, 'value_'+i)
            }

            map.sort(true);
            console.log('ascending order...');
            console.log(map.getEntries());

            map.each()
            for(var i=0;i<50;i++) {
                var exp1 = map.getEntryAt(i).key;
                var exp2 = i;
                passed = assert.areEqual('The keys are not in ascending order.', exp1, exp2);
            }

            map.sort(false);

            var on = 49;
            for(var i=0;i<50;i++) {
                var exp1 = map.getEntryAt(i).key;
                var exp2 = (on-i);
                passed = assert.areEqual('The keys are not in descending order.', exp1, exp2);
            }

            console.log('descending order...');
            console.log(map.getEntries());
            assert.passed('test.sort', passed);
        },
        each: function(){
            console.log('****************** each test started ******************');
            var passed = true;
            var map = new ObjectMap('number', 'string', true, false);

            for(var i=0;i<50;i++) {
                map.put(i, 'value_'+i)
            }

            var stopAt = 20;
            var stop = 0;

            map.each(function(entry, index){
                var exp2 = index;
                passed = assert.areEqual('The keys are not in ascending order.', entry.key, exp2);
                stop=index;
                return !(index===stopAt);
            });

            assert.isTrue("Did not stop on expected entry.", stop===stopAt);

            map.each(function(entry, index){
                stop=index;
            });

            assert.isTrue("Did not iterate the entire map.", (stop===(map.size()-1)));

            assert.passed('test.each', passed);
        }
    }
};

var assert ={
    areEqual: function(errorMsg, arg1, arg2){
        return this.isTrue(errorMsg, arg1===arg2);
    },
    isTrue: function(errorMsg, statement){
        if(!statement){
            throw errorMsg;
        }
        return statement;
    },
    passed: function(message, statement){
        function log(msg, color) {
            console.log("%c" + msg, "color:" + color + ";font-weight:bold;");
        }
        log(message + ' ' + (statement? 'passed.': 'failed.'), statement ? 'green' : 'red');
    }
};